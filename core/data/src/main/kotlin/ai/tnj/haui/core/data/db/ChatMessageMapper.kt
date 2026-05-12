package ai.tnj.haui.core.data.db

import ai.tnj.haui.core.data.db.entity.ChatMessageEntity
import ai.tnj.haui.core.model.ChatMessage
import android.net.Uri

fun ChatMessage.toEntity(sessionId: String, createdAt: Long): ChatMessageEntity =
    ChatMessageEntity(
        id = id,
        sessionId = sessionId,
        role = role,
        type = type,
        text = text,
        imageUri = imageUri?.toString(),
        mimeType = mimeType,
        fileName = fileName,
        createdAt = createdAt,
    )

fun ChatMessageEntity.toModel(): ChatMessage =
    ChatMessage(
        id = id,
        role = role,
        type = type,
        text = text,
        imageUri = imageUri?.let(Uri::parse),
        mimeType = mimeType,
        fileName = fileName,
    )
