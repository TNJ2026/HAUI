package ai.tnj.haui.core.model

import android.net.Uri

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val type: ChatMessageType,
    val text: String,
    val imageUri: Uri? = null,
    val mimeType: String? = null,
    val fileName: String? = null
)

enum class ChatMessageType {
    Text,
    Image,
    Document,
    Combination,
    Error;

    companion object {
        fun determine(text: String, attachmentMimeType: String?): ChatMessageType {
            return when {
                attachmentMimeType == null -> Text
                text.isNotBlank() -> Combination
                attachmentMimeType.startsWith("image/") -> Image
                else -> Document
            }
        }
    }
}