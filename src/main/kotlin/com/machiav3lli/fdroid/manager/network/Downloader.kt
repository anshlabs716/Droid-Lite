package com.machiav3lli.fdroid.manager.network

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.CLIENT_CONNECT_TIMEOUT_MS
import com.machiav3lli.fdroid.CLIENT_USER_AGENT
import com.machiav3lli.fdroid.POOL_DEFAULT_KEEP_ALIVE_DURATION_M
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.DownloadTask
import com.machiav3lli.fdroid.utils.dmReasonToHttpResponse
import com.machiav3lli.fdroid.utils.extension.text.formatDateTime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.cancellation.CancellationException

object Downloader {
    private const val TAG = "Downloader"
    private val retries = ConcurrentHashMap<String, AtomicInteger>()
    private val downloadSemaphore = Semaphore(Preferences[Preferences.Key.MaxParallelDownloads])
    private val clients = ConcurrentHashMap<String, OkHttpClient>()

    var proxy: Proxy? = null
        set(value) {
            Log.i(this.javaClass.name, "updating main proxy to $value")
            if (field != value) {
                field = value
                clients.clear()
            }
        }

    class Result(val statusCode: HttpStatus, val lastModified: String, val entityTag: String) {
        val success: Boolean
            get() = statusCode.isSuccess

        val isNotModified: Boolean
            get() = statusCode == HttpStatus.NotModified
    }

    private fun getProxy(onion: Boolean): Proxy? =
        if (onion) Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 9050))
        else proxy

    private fun clientFor(url: String): OkHttpClient = synchronized(clients) {
        clients.getOrPut(url) { createDownloadClient(getProxy(url.endsWith(".onion"))) }
    }

    suspend fun download(
        url: String,
        mirrorUrls: List<String> = listOf(url),
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        rated: Boolean = true,
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
    ): Result = if (rated) downloadSemaphore.withPermit {
        Log.i(
            TAG,
            "Entering download of $url.\nPermissions left for parallel downloads: ${downloadSemaphore.availablePermits}"
        )
        permittedDownload(url, mirrorUrls, target, lastModified, entityTag, authentication, callback)
    } else permittedDownload(url, mirrorUrls, target, lastModified, entityTag, authentication, callback)

    private suspend fun permittedDownload(
        url: String,
        mirrorUrls: List<String> = listOf(url),
        target: File,
        lastModified: String,
        entityTag: String,
        authentication: String,
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
    ): Result = coroutineScope {
        ensureActive()

        val start = if (target.exists()) target.length().coerceAtLeast(0L) else null
        Log.i(TAG, "download start byte = $start")

        try {
            val request = Request.Builder().url(url).apply {
                header("User-Agent", CLIENT_USER_AGENT)
                if (lastModified.isNotEmpty())
                    header("If-Modified-Since", lastModified)
                if (authentication.isNotEmpty())
                    header("Authorization", authentication)
                header("Cache-Control", "max-age=60")
                if (entityTag.isNotEmpty())
                    header("If-None-Match", entityTag)
                if (start != null)
                    header("Range", "bytes=$start-")
            }.build()

            clientFor(url).newCall(request).execute().use { response ->
                when {
                    response.code == 304 -> {
                        retries.remove(url)
                        Result(HttpStatus.NotModified, lastModified, entityTag)
                    }

                    response.code in 200..299 -> {
                        val contentRange = response.header("Content-Range")
                        val append = start != null && contentRange != null
                        val body = response.body ?: throw HttpResponseException(
                            "Empty body.", HttpStatus(response.code)
                        )
                        val declaredLength = contentRange
                            ?.substringAfterLast('/')
                            ?.toLongOrNull()
                            ?: body.contentLength().takeIf { it > 0 }
                        val progressStart = start ?: 0L

                        FileOutputStream(target, append).use { output ->
                            body.byteStream().use { input ->
                                val buffer = ByteArray(BUFFER_SIZE)
                                var readTotal = 0L
                                while (true) {
                                    ensureActive()
                                    val read = input.read(buffer)
                                    if (read == -1) break
                                    readTotal += read
                                    if (declaredLength != null && readTotal > declaredLength)
                                        throw DownloadSizeException(
                                            "Downloaded size exceeds expected total size"
                                        )
                                    output.write(buffer, 0, read)
                                    callback(progressStart + readTotal, declaredLength, -1L)
                                }
                                output.fd.sync()
                            }
                        }

                        retries.remove(url)
                        Result(HttpStatus(response.code), lastModified, entityTag)
                    }

                    response.code == 416 -> {
                        Log.w(
                            TAG,
                            "Failed to download file ($url) with Range: ${start?.let { "bytes=$it-" }}."
                        )
                        target.delete()
                        permittedDownload(
                            url = url,
                            target = target,
                            lastModified = lastModified,
                            entityTag = entityTag,
                            authentication = authentication,
                            callback = callback,
                        )
                    }

                    response.code == 504 || response.code == 408 -> {
                        throw HttpResponseException(
                            "Failed to download file.",
                            HttpStatus(response.code),
                        )
                    }

                    response.code == 404 -> Result(HttpStatus.NotFound, "", "")

                    else -> {
                        Log.w(
                            TAG,
                            "Failed to download file ($url). Response code ${response.code}."
                        )
                        throw HttpResponseException(
                            "Failed to download file.",
                            HttpStatus(response.code),
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            retries.remove(url)
            throw e
        } catch (e: DownloadSizeException) {
            throw e
        } catch (e: Exception) {
            val leftRetries = retries.getOrPut(url) { AtomicInteger(3) }
            Log.w(
                TAG,
                "Download ($url) faced exception. Tries left: $leftRetries. Exception: ${e.message}."
            )
            if (leftRetries.decrementAndGet() > 0) {
                retries[url] = leftRetries
                permittedDownload(
                    url = mirrorUrls.minus(url).randomOrNull() ?: url,
                    mirrorUrls = mirrorUrls,
                    target = target,
                    lastModified = lastModified,
                    entityTag = entityTag,
                    authentication = authentication,
                    callback = callback,
                )
            } else if (e is java.net.SocketTimeoutException)
                throw HttpResponseException(
                    e.message ?: "Network Error",
                    HttpStatus.RequestTimeout,
                    e
                )
            else throw e
        }
    }

    suspend fun dmDownload(
        context: Context,
        task: DownloadTask,
        target: File,
        callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit,
    ): Result = coroutineScope {
        val start = if (target.exists()) target.length().coerceAtLeast(0L)
        else null
        val progressStart = start ?: 0L

        val request = DownloadManager.Request(task.url.toUri())
            .setTitle("${task.name} (${task.release.version})")
            .setDescription(task.packageName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationUri(target.toUri())
            .apply {
                addRequestHeader("Authorization", task.authentication)
                addRequestHeader("Accept-Encoding", "gzip, deflate")
                addRequestHeader("User-Agent", CLIENT_USER_AGENT)
            }
        val downloadManager =
            ContextCompat.getSystemService(context, DownloadManager::class.java)
        val downloadID = downloadManager?.enqueue(request) ?: -1L

        var isDownloading = true
        var downloadStatus: Int
        var progressRead: Long
        var progressTotal: Long?
        var response: HttpStatus
        var lastModified: Long?
        var cursor: Cursor

        do {
            val downloadQuery = DownloadManager.Query()
            downloadQuery.setFilterById(downloadID)
            cursor = downloadManager!!.query(downloadQuery)
            cursor.moveToFirst()
            progressRead = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )?.toLong() ?: 0L
            progressTotal = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )?.toLong()

            // Check if downloaded size exceeds total size
            if (progressTotal != null && progressTotal != -1L && progressStart + progressRead > progressTotal) {
                downloadManager.remove(downloadID)
                throw DownloadSizeException("Downloaded size exceeds expected total size")
            }

            lastModified = cursor.getLongOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)
            )
            val responseStatus = cursor.getIntOrNull(
                cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            )
            downloadStatus =
                cursor.getIntOrNull(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    ?: DownloadManager.STATUS_PENDING

            response = responseStatus?.dmReasonToHttpResponse() ?: HttpStatus.OK

            callback.invoke(progressStart + progressRead, progressTotal, downloadID)

            when (downloadStatus) {
                DownloadManager.STATUS_SUCCESSFUL,
                DownloadManager.STATUS_FAILED,
                    -> isDownloading = false
            }

            cursor.close()
            if (isDownloading) delay(100L) // avoid busy-waiting
        } while (isDownloading)

        // Final check after download completion
        if (progressTotal != null && progressTotal != -1L && target.length() > progressTotal) {
            downloadManager.remove(downloadID)
            throw DownloadSizeException("Downloaded size exceeds expected total size")
        }

        Result(response, lastModified?.formatDateTime() ?: "", "")
    }
}

private fun createDownloadClient(proxy: Proxy?): OkHttpClient = OkHttpClient.Builder()
    .connectionPool(
        ConnectionPool(
            Preferences[Preferences.Key.MaxIdleConnections],
            POOL_DEFAULT_KEEP_ALIVE_DURATION_M,
            TimeUnit.MINUTES
        )
    )
    .connectTimeout(CLIENT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    .readTimeout(CLIENT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    .writeTimeout(CLIENT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    .retryOnConnectionFailure(true)
    .followRedirects(true)
    .followSslRedirects(true)
    .proxy(proxy)
    .connectionSpecs(
        listOf(
            ConnectionSpec.RESTRICTED_TLS,
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.CLEARTEXT
        )
    )
    .apply {
        if (Preferences[Preferences.Key.DisableCertificateValidation]) {
            val trustAllCerts = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            sslSocketFactory(sslSocketFactory, trustAllCerts)
            hostnameVerifier { _, _ -> true }
        }
    }
    .build()

class DownloadSizeException(message: String, cause: Throwable? = null) : Exception(message, cause)
class HttpResponseException(
    message: String,
    val responseStatus: HttpStatus,
    cause: Throwable? = null
) : Exception(message, cause)
