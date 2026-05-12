package ai.tnj.haui.core.data.db.entity

import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.MessageRole
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index("sessionId", "createdAt")]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: MessageRole,
    val type: ChatMessageType,
    val text: String,
    val imageUri: String?,
    val mimeType: String?,
    val fileName: String?,
    val createdAt: Long,
)
