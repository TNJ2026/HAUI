package ai.tnj.haui.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class OpenAIResponsesRequestBody(
   val input: String,
   val instructions: String? = null,
   @SerialName("previous_response_id") val previousResponseId: String? = null,
   val conversation: String? = null,
   @SerialName("conversation_history") val conversationHistory: List<String>? = null,
   val store: Boolean = true,
)

@Serializable
data class ChatCompletionsMessage(
   val content: List<ChatCompletionsMessageContent>,
   val role: String = "user"
)

@Serializable
data class ChatCompletionsMessageContent(
   val type: String,
   val text: String? = null,
   @SerialName("image_url") val imageUrl: ImageContent? = null
)

@Serializable
data class ImageContent(
   val url: String
)

@Serializable
data class ChatCompletionsRequestBody(
   val messages: List<ChatCompletionsMessage>,
   val stream: Boolean = true,
   val model: String = "hermes-agent"
)

@Serializable
data class RunRequestBody(
   val input: String,
   val instructions: String? = null,
   @SerialName("previous_response_id") val previousResponseId: String? = null,
   @SerialName("conversation_history") val conversationHistory: List<String>? = null,
   @SerialName("session_id") val sessionId: String? = null,
)