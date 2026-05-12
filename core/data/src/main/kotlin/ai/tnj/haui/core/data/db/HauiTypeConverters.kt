package ai.tnj.haui.core.data.db

import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.MessageRole
import androidx.room.TypeConverter

class HauiTypeConverters {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole =
        runCatching { enumValueOf<MessageRole>(value) }.getOrDefault(MessageRole.USER)

    @TypeConverter
    fun fromChatMessageType(type: ChatMessageType): String = type.name

    @TypeConverter
    fun toChatMessageType(value: String): ChatMessageType =
        runCatching { enumValueOf<ChatMessageType>(value) }.getOrDefault(ChatMessageType.Text)
}
