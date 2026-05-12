package ai.tnj.haui.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HermesCompletionsMessage(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>? = null,
    val usage: TokenUsage? = null
) {

    fun displayText(): String {
        val list = choices ?: return ""
        if (list.isEmpty()) return ""
        return list.first().delta.content ?: ""
    }

    @Serializable
    data class TokenUsage(
        @SerialName("prompt_tokens") val promptTokens: Int,
        @SerialName("completion_tokens")  val completionTokens: Int,
        @SerialName("total_tokens")  val totalTokens: Int
    )

    @Serializable
    data class Choice(
        val index: Int,
        val delta: Delta,
        @SerialName("finish_reason") val finishReason: String?
    )

    @Serializable
    data class Delta(
        val content: String? = null,
        val role: String? = null
    )
}

@Serializable
data class HermesCompletionsTools(
    val tool: String,
    val emoji: String?,
    val label: String?,
    val toolCallId: String?,
    val status: String?
) {

    fun displayText(): String {
        val text = if (tool == label) {
            "${emoji ?: ""}$tool ${toolCallId ?: ""} ${status ?: ""}"
        } else {
            "${emoji ?: ""}$tool  ${label ?: ""} ${toolCallId ?: ""} ${status ?: ""}"
        }
        return text.trim()
    }
}
