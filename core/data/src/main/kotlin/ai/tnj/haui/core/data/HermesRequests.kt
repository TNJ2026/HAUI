package ai.tnj.haui.core.data

import ai.tnj.haui.core.model.ChatCompletionsMessage
import ai.tnj.haui.core.model.ChatCompletionsMessageContent
import ai.tnj.haui.core.model.ChatCompletionsRequestBody
import ai.tnj.haui.core.model.HauiJson
import ai.tnj.haui.core.model.ImageContent
import ai.tnj.haui.core.utils.PendingAttachment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Builds raw OkHttp [Request]s for Hermes SSE endpoints. Plain JSON endpoints
 * have moved to Retrofit (`HermesService`); this class is only kept for the
 * streaming paths that Retrofit cannot model.
 */
class HermesRequests(private val baseUrl: String, private val apiKey: String = "") {

    fun runEvents(runId: String): Request =
        commonRequestBuilder("v1/runs/$runId/events").build()

    fun chatCompletions(
        input: String,
        sessionId: String?,
        attachment: PendingAttachment?,
        stream: Boolean = true
    ): Request {
        val messageContents = mutableListOf<ChatCompletionsMessageContent>()
        if (input.isNotBlank()) {
            messageContents.add(ChatCompletionsMessageContent(text = input.trim(), type = "text"))
        }
        if (attachment != null) {
            when (attachment) {
                is PendingAttachment.Image -> {
                    messageContents.add(
                        ChatCompletionsMessageContent(
                            type = "image_url",
                            imageUrl = ImageContent(url = attachment.base64)
                        )
                    )
                }
                is PendingAttachment.Document -> {
                    val content = attachment.text ?: "data:${attachment.mimeType};base64,${attachment.base64}"
                    messageContents.add(
                        ChatCompletionsMessageContent(
                            type = "text",
                            text = content
                        )
                    )
                }
            }
        }
        val messages = listOf(
            ChatCompletionsMessage(messageContents)
        )
        val requestBody = ChatCompletionsRequestBody(
            messages = messages,
            stream = stream,
        )
        return commonRequestBuilder("v1/chat/completions")
            .apply {
                sessionId?.takeIf { it.isNotBlank() }
                    ?.let { addHeader("X-Hermes-Session-Id", it) }
            }
            .post(HauiJson.encodeToString(requestBody).toRequestBody(JSON_MEDIA_TYPE))
            .build()
    }

    private fun commonRequestBuilder(path: String): Request.Builder =
        Request.Builder()
            .url("$baseUrl/$path")
            .apply { if (apiKey.isNotEmpty()) addHeader("Authorization", "Bearer $apiKey") }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
