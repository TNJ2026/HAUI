package ai.tnj.haui.core.data.chat

import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.network.SseEvent
import ai.tnj.haui.core.utils.PendingAttachment

/**
 * Common contract for the two chat backends:
 *
 *  - [ChatCompletionsHandler] uses `/v1/chat/completions` (streaming SSE).
 *  - [ChatRunsHandler] uses the legacy `/v1/runs` flow with `runEvents`.
 *
 * `ChatTab` consumes a [ChatPresenter] without knowing which implementation
 * is active; the active one is selected at composition time based on the
 * persisted [ai.tnj.haui.feature.home.ui.settings.ChatProtocol] setting.
 */
interface ChatPresenter {

    suspend fun sendChat(
        text: String,
        attachment: PendingAttachment? = null
    )

    fun clear()

    /**
     * Server-assigned identifier that ties this conversation together —
     * `currentSessionId` for chat-completions (from `X-Hermes-Session-Id`) or
     * `currentRunId` for runs. Returns `null` until the first request lands.
     */
    fun currentServerId(): String?

    /** Rehydrate the handler with a previously persisted server id. */
    fun setCurrentServerId(id: String?)
}


/**
 * Write side of the chat message channel. Handlers push parsed SSE events
 * into the sink; the implementation (typically [ai.tnj.haui.feature.home.ui.chat.ChatViewModel]) is responsible
 * for fanning the change into the UI state.
 */
interface ChatMessageSink {

    fun onNewMessage(message: ChatMessage)

    fun onNewMessage(index: Int, message: ChatMessage)

    fun onMessageUpdate(message: ChatMessage)

    fun onError(event: SseEvent.Failure)

    fun onMessageEnd()

    /**
     * Called whenever the backend reports cumulative token usage for the
     * current chat session. Implementations typically forward this to the
     * UI state so the composer can render a context-window progress bar.
     */
    fun onUsageUpdate(totalTokens: Int)
}


/**
 * Read side of the chat message buffer. Handlers query it while merging
 * streaming deltas (e.g. "find the previous chunk for this id and append").
 * Kept separate from [ChatMessageSink] so consumers depend only on what they
 * actually need (Interface Segregation).
 */
interface ChatMessageStore {

    fun contains(id: String): Boolean

    fun indexOf(id: String): Int

    fun find(id: String): ChatMessage?
}


/**
 * Cumulative token usage as exposed to the UI. [progress] is the fraction of
 * the assumed context window that has been consumed, clamped to `[0, 1]`.
 */
data class TokenUsageInfo(
    val totalTokens: Int
) {
    val progress: Float
        get() = if (totalTokens <= 0) {
            0f
        } else {
            (totalTokens.toFloat() / DEFAULT_MAX_CONTEXT_TOKENS).coerceIn(0f, 1f)
        }

    companion object {
        /**
         * Default context-window estimate when the backend doesn't expose one.
         * 32k tokens covers the common 32k/128k models reasonably well; if the
         * server returns a usage already > this value the bar saturates at 1f.
         */
        const val DEFAULT_MAX_CONTEXT_TOKENS = 204.8f * 1000
    }
}


sealed interface ChatUiState {

    data class ChatUIData(
        val messages: List<ChatMessage>,
        val healthOk: Boolean = false,
        val hasPendingRun: Boolean = false,
        val showTypingIndicator: Boolean = false,
        val tokenUsage: TokenUsageInfo? = null,
    ) : ChatUiState

    data object Empty : ChatUiState
}
