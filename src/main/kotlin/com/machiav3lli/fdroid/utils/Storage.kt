package com.machiav3lli.fdroid.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.machiav3lli.fdroid.data.content.Preferences
import java.io.File

private const val TAG = "util.Storage"

fun Context.getDownloadFolder(): SafTree? =
    SafTree.from(this, Preferences[Preferences.Key.DownloadDirectory])

fun SafTree.copyIn(cacheFile: File): Boolean {
    val copied = copyIn(cacheFile, cacheFile.name)
    when {
        copied -> Log.d(TAG, "Copied ${cacheFile.name} to download folder")
        else   -> Log.e(TAG, "Failed to copy ${cacheFile.name} to download folder")
    }
    return copied
}

val DOWNLOAD_DIRECTORY_INTENT = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

val isDownloadExternal: Boolean
    get() = Preferences[Preferences.Key.EnableDownloadDirectory] && Preferences[Preferences.Key.DownloadDirectory] != ""
