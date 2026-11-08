package com.machiav3lli.fdroid.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Minimal SAF tree-directory helper replacing the simple-storage dependency.
 * Supports the three operations Droid-Lite needs: list, delete, stream copy.
 */
class SafTree(private val context: Context, val treeUri: Uri) {

    private val docUri: Uri by lazy {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
    }

    private fun childUri(docId: String): Uri =
        DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

    private fun queryChildren(): List<Child> = try {
        val resolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getDocumentId(docUri)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        resolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val name = cursor.getString(1)
                    val mime = cursor.getString(2)
                    if (id != null && name != null)
                        add(Child(id, name, mime == DocumentsContract.Document.MIME_TYPE_DIR))
                }
            }
        } ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    data class Child(val documentId: String, val name: String, val isDirectory: Boolean)

    fun list(): List<Child> = queryChildren()

    fun find(name: String): Child? = queryChildren().find { it.name == name }

    fun delete(child: Child): Boolean = try {
        context.contentResolver.delete(childUri(child.documentId), null, null) != 0
    } catch (e: Exception) {
        false
    }

    fun deleteByName(name: String): Boolean {
        val child = find(name) ?: return false
        return delete(child)
    }

    fun openInput(child: Child): InputStream? =
        context.contentResolver.openInputStream(childUri(child.documentId))

    fun openOutput(name: String, mime: String = "application/octet-stream"): OutputStream? {
        val target = find(name)
        val uri = if (target != null && !target.isDirectory)
            childUri(target.documentId)
        else try {
            DocumentsContract.createDocument(context.contentResolver, docUri, mime, name)
        } catch (e: Exception) {
            null
        } ?: return null
        return context.contentResolver.openOutputStream(uri)
    }

    fun copyIn(source: File, name: String): Boolean = try {
        openOutput(name)?.use { out -> source.inputStream().use { it.copyTo(out) } } != null
    } catch (e: Exception) {
        false
    }

    companion object {
        fun from(context: Context, uriString: String): SafTree? = try {
            val uri = Uri.parse(uriString)
            val docId = DocumentsContract.getTreeDocumentId(uri)
            if (docId.isNullOrEmpty()) null else SafTree(context, uri)
        } catch (e: Exception) {
            null
        }
    }
}
