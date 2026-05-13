package ai.tnj.haui.core.data.chat



import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.HauiJson
import ai.tnj.haui.core.model.HermesChatMsgEvent
import ai.tnj.haui.core.model.HermesMessageData
import ai.tnj.haui.core.model.HermesResponse
import ai.tnj.haui.core.model.MessageRole
import ai.tnj.haui.core.network.SseEvent
import ai.tnj.haui.core.utils.LogUtil
import ai.tnj.haui.core.utils.PendingAttachment
import java.util.UUID


class ChatRunsHandler(
    private val hermesRepository: HermesRepository,
    private val sink: ChatMessageSink,
    private val store: ChatMessageStore,
): ChatPresenter {

    companion object {
        const val TAG = "ChatRunsHandler"
    }

    private var currentRunId: String? = null
    private var currentMsgId: String = ""
    private var currentToolMsgId: String = ""

    override fun clear() {
        currentRunId = null
        currentMsgId = ""
        currentToolMsgId = ""
    }

    override fun currentServerId(): String? = currentRunId

    override fun setCurrentServerId(id: String?) {
        currentRunId = id
    }

    /**
     * Initiates a chat run with the provided text and optional attachments,
     * then observes the resulting stream of events. not support attachment.
     */
    override suspend fun sendChat(
        text: String,
        attachment: PendingAttachment?
    ) {
        hermesRepository.runs(text, currentRunId).collect { result ->
            when (result) {
                is HermesResponse.Success -> {
                    val newRunId = result.data.runId
                    if (currentRunId != newRunId) {
                        currentRunId = newRunId
                    }
                    observeRunEvents(newRunId)
                }
                is HermesResponse.Error -> {
                    LogUtil.e(TAG, "sendChat error: ${result.exception.code} ${result.exception.msg}")
                    sink.onError(SseEvent.Failure(result.exception, null))
                }
                is HermesResponse.Loading -> {}
            }
        }
    }

    private suspend fun observeRunEvents(runId: String) {
        try {
            hermesRepository.runEvents(runId).collect { event ->
                LogUtil.d(TAG) { "observeRunEvents event: $event" }
                when (event) {
                    is SseEvent.Open -> LogUtil.d(TAG) { "observeRunEvents headers: ${event.response.headers}" }
                    is SseEvent.Message -> handleMessage(event.data)
                    is SseEvent.Failure -> {
                        currentMsgId = ""
                        sink.onError(event)
                    }
                    is SseEvent.Closed -> sink.onMessageEnd()
                }
            }
        } catch (e: Throwable) {
            LogUtil.e(TAG, "observeRunEvents error", e)
            sink.onError(SseEvent.Failure(e, null))
        }
    }

    private fun handleMessage(message: String) {
        try {
            val messageData = HauiJson.decodeFromString<HermesMessageData>(message)
            messageData.usage?.let { sink.onUsageUpdate(it.totalTokens) }
            when(messageData.event) {
                HermesChatMsgEvent.ToolsStart.eventName,HermesChatMsgEvent.ToolsCompleted.eventName -> {
                    handleToolsMsg(messageData)
                }
                HermesChatMsgEvent.Delta.eventName -> {
                    handleDeltaMessage(messageData)
                }
                else -> {
                    handleOtherMessage(messageData)
                }
            }
        } catch (e: Throwable) {
            LogUtil.e(TAG, "handleMessage error", e)
        }
    }

    private fun handleOtherMessage(messageData: HermesMessageData) {
        val currentMsg = store.find(currentMsgId)
        if (currentMsg == null) {
            addMessage(currentMsgId, messageData, isGenerating = true)
            return
        }
        val isCompleted = messageData.event == HermesChatMsgEvent.Completed.eventName
        sink.onMessageUpdate(currentMsg.copy(text = messageData.displayText(), isGenerating = !isCompleted))
        if (isCompleted) {
            currentMsgId = ""
        }
    }

    private fun handleDeltaMessage(messageData: HermesMessageData) {
        if (currentMsgId.isEmpty()) {
            currentMsgId = UUID.randomUUID().toString()
            addMessage(currentMsgId, messageData, isGenerating = true)
            return
        }
        val currentMsg = store.find(currentMsgId)
        if (currentMsg != null) {
            sink.onMessageUpdate(currentMsg.copy(text = currentMsg.text + messageData.displayText(), isGenerating = true))
        }
    }

    private fun handleToolsMsg(messageData: HermesMessageData) {
        if (messageData.event == HermesChatMsgEvent.ToolsStart.eventName) {
            currentToolMsgId = UUID.randomUUID().toString()
            val message = ChatMessage(
                id = currentToolMsgId,
                role = MessageRole.TOOL,
                type = ChatMessageType.Text,
                text = messageData.displayText(),
                isGenerating = true
            )
            val index = store.indexOf(currentMsgId)
            if (index == -1) {
                sink.onNewMessage(message)
            } else {
                sink.onNewMessage(index, message)
            }
            return
        }
        val currentMsg = store.find(currentToolMsgId)
        val isCompleted = messageData.event == HermesChatMsgEvent.ToolsCompleted.eventName
        if (currentMsg != null) {
           sink.onMessageUpdate(currentMsg.copy(text = currentMsg.text + messageData.displayText(), isGenerating = !isCompleted))
        }
        if (isCompleted) {
            currentToolMsgId = ""
        }
    }

    private fun addMessage(id: String, messageData: HermesMessageData, isGenerating: Boolean = false) {
        val message = ChatMessage(
            id = id,
            role = MessageRole.ASSISTANT,
            type = ChatMessageType.Text,
            text = messageData.displayText(),
            isGenerating = isGenerating
        )
        sink.onNewMessage(message)
    }
}