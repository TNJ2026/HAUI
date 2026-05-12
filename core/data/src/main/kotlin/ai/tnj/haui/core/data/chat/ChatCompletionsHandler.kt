package ai.tnj.haui.core.data.chat


import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.HauiJson
import ai.tnj.haui.core.model.HermesCompletionsMessage
import ai.tnj.haui.core.model.HermesCompletionsTools
import ai.tnj.haui.core.model.MessageRole
import ai.tnj.haui.core.network.SseEvent
import ai.tnj.haui.core.utils.LogUtil
import ai.tnj.haui.core.utils.PendingAttachment
import java.util.UUID


class ChatCompletionsHandler(
    private val hermesRepository: HermesRepository,
    private val sink: ChatMessageSink,
    private val store: ChatMessageStore,
): ChatPresenter {

    companion object {
        const val TAG = "ChatCompletionsHandler"
        private const val DONE_SENTINEL = "[DONE]"
    }

    private var currentMsgId = ""
    private var currentSessionId: String? = null

    override fun clear() {
        currentMsgId = ""
        currentSessionId = null
    }

    override fun currentServerId(): String? = currentSessionId

    override fun setCurrentServerId(id: String?) {
        currentSessionId = id
    }

    override suspend fun sendChat(
        text: String,
        attachment: PendingAttachment?
    ) {
        try {
            hermesRepository.chatCompletionsStream(
                text,
                currentSessionId,
                attachment
            ).collect { event ->
                LogUtil.d(TAG) { "chatCompletionsStream:$event" }
                when (event) {
                    is SseEvent.Open -> {
                        val sessionId = event.response.header("X-Hermes-Session-Id") ?: ""
                        if (currentSessionId != sessionId) {
                            currentSessionId = sessionId
                        }
                    }
                    is SseEvent.Message -> handleMessage(event.type ?: "", event.data)
                    is SseEvent.Failure -> {
                        currentMsgId = ""
                        sink.onError(event)
                    }
                    is SseEvent.Closed -> {
                        currentMsgId = ""
                        sink.onMessageEnd()
                    }
                }
            }
        } catch (e: Throwable) {
            LogUtil.e(TAG, "sendChat error", e)
            sink.onError(SseEvent.Failure(e, null))
        }
    }


    private fun handleMessage(type: String, message: String) {
        try {
            if (type == ChatCompletionsMessageType.Tool.typeName) {
                val toolsMsg = HauiJson.decodeFromString<HermesCompletionsTools>(message)
                val chatMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.TOOL,
                    type = ChatMessageType.Text,
                    text = toolsMsg.displayText()
                )
                val index = store.indexOf(currentMsgId)
                if (index == -1) {
                    sink.onNewMessage(chatMessage)
                } else {
                    sink.onNewMessage(index, chatMessage)
                }
                return
            }
            if (message.trim() == DONE_SENTINEL) return
            val messageData = HauiJson.decodeFromString<HermesCompletionsMessage>(message)
            messageData.usage?.let { sink.onUsageUpdate(it.totalTokens) }
            handleDeltaMessage(messageData)
        } catch (e: Throwable) {
            LogUtil.e(TAG, "handleMessage error", e)
        }
    }

    private fun handleDeltaMessage(messageData: HermesCompletionsMessage) {
        val text = messageData.displayText()
        if (text.isBlank()) return
        if (store.contains(messageData.id)) {
            val oldMessage = store.find(messageData.id) ?: return
            sink.onMessageUpdate(oldMessage.copy(text = oldMessage.text + text))
        } else {
            currentMsgId = messageData.id
            val message = ChatMessage(
                id = messageData.id,
                role = MessageRole.ASSISTANT,
                type = ChatMessageType.Text,
                text = text
            )
            sink.onNewMessage(message)
        }
    }
}

enum class ChatCompletionsMessageType(val typeName: String) {
    Tool("hermes.tool.progress"),
}