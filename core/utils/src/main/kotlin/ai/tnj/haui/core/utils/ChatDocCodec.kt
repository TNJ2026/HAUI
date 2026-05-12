package ai.tnj.haui.core.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64

fun loadDocumentAttachment(resolver: ContentResolver, uri: Uri): PendingAttachment {
    val mimeType = resolver.getType(uri) ?: "application/octet-stream"
    val fileName = queryDocumentDisplayName(resolver, uri)
        ?: uri.lastPathSegment?.substringAfterLast('/')
        ?: "document"

    // Check file size (limit to 5MB)
    val fileSize = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
    if (fileSize > 5 * 1024 * 1024) {
        throw IllegalStateException("File size exceeds 5MB limit")
    }

    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException("unable to read document")

    // Optimization: Check if it's likely a text file vs binary
    val isText = mimeType.startsWith("text/") || 
                 mimeType == "application/json" || 
                 mimeType == "application/xml"

    return if (isText) {
        PendingAttachment.Document(
            id = uri.toString() + "#" + System.currentTimeMillis().toString(),
            uri = uri,
            fileName = fileName,
            mimeType = mimeType,
            text = String(bytes, Charsets.UTF_8)
        )
    } else {
        PendingAttachment.Document(
            id = uri.toString() + "#" + System.currentTimeMillis().toString(),
            uri = uri,
            fileName = fileName,
            mimeType = mimeType,
            base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        )
    }
}

private fun queryDocumentDisplayName(resolver: ContentResolver, uri: Uri): String? {
    return resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}
