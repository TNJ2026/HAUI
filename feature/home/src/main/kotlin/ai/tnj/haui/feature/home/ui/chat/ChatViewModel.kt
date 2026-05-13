package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.data.LocalDataStore
import ai.tnj.haui.core.data.chat.ChatCompletionsHandler
import ai.tnj.haui.core.data.chat.ChatMessageSink
import ai.tnj.haui.core.data.chat.ChatMessageStore
import ai.tnj.haui.core.data.chat.ChatPresenter
import ai.tnj.haui.core.data.chat.ChatRunsHandler
import ai.tnj.haui.core.data.chat.ChatUiState
import ai.tnj.haui.core.data.chat.TokenUsageInfo
import ai.tnj.haui.core.data.db.dao.SessionSummary
import ai.tnj.haui.core.data.di.IoDispatcher
import ai.tnj.haui.core.data.repository.ChatHistoryRepository
import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.HermesResponse
import ai.tnj.haui.core.model.MessageRole
import ai.tnj.haui.core.network.SseEvent
import ai.tnj.haui.core.utils.PendingAttachment
import ai.tnj.haui.feature.home.ui.settings.ChatProtocol
import ai.tnj.haui.feature.home.ui.settings.parseChatProtocol
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Owns the chat screen's UI state and the active [ChatPresenter].
 * Handles message routing, state synchronization, and persistence.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val localDataStore: LocalDataStore,
    private val hermesRepository: HermesRepository,
    private val chatHistoryRepository: ChatHistoryRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private data class InternalChatState(
        val messages: List<ChatMessage> = emptyList(),
        val hasPendingRun: Boolean = false,
        val healthOk: Boolean = true,
        val showTypingIndicator: Boolean = false,
        val tokenUsage: TokenUsageInfo? = null,
    )

    private val _internalState = MutableStateFlow(InternalChatState())
    
    @OptIn(FlowPreview::class)
    val uiState: StateFlow<ChatUiState> = _internalState
        .sample(50L)
        .map { s ->
            ChatUiState.ChatUIData(
                messages = s.messages,
                healthOk = s.healthOk,
                hasPendingRun = s.hasPendingRun,
                showTypingIndicator = s.showTypingIndicator,
                tokenUsage = s.tokenUsage,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChatUiState.ChatUIData(messages = emptyList()),
        )

    val protocol: StateFlow<ChatProtocol> = localDataStore.chatProtocol
        .map(::parseChatProtocol)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = parseChatProtocol(localDataStore.chatProtocol.value),
        )

    val showToolBubble: StateFlow<Boolean> = localDataStore.showToolBubble

    private var chatHandler: ChatPresenter? = null
    private var chatCompletionsHandler: ChatCompletionsHandler? = null
    private var chatRunsHandler: ChatRunsHandler? = null

    private var streamJob: Job? = null
    private var healthJob: Job? = null
    private var skipNextProtocolReset = false

    private val messageBridge = object : ChatMessageSink, ChatMessageStore {
        override fun onNewMessage(message: ChatMessage) {
            _internalState.update { s ->
                val hiddenTool = message.role == MessageRole.TOOL && !showToolBubble.value
                s.copy(
                    messages = s.messages + message,
                    showTypingIndicator = if (hiddenTool) s.showTypingIndicator else false
                )
            }
        }

        override fun onNewMessage(index: Int, message: ChatMessage) {
            _internalState.update { s ->
                s.copy(messages = s.messages.toMutableList().apply { add(index, message) })
            }
        }

        override fun onMessageUpdate(message: ChatMessage) {
            _internalState.update { s ->
                val idx = s.messages.indexOfFirst { it.id == message.id }
                if (idx == -1) s else s.copy(
                    messages = s.messages.toMutableList().apply { set(idx, message) }
                )
            }
        }

        override fun onError(event: SseEvent.Failure) {
            _internalState.update { it.copy(hasPendingRun = false, showTypingIndicator = false) }
            appendErrorMessage(event)
        }

        override fun onMessageEnd() {
            _internalState.update { it.copy(
                hasPendingRun = false, 
                showTypingIndicator = false,
                messages = it.messages.map { msg -> 
                    if (msg.isGenerating) msg.copy(isGenerating = false) else msg 
                }
            ) }
            persistCurrentConversation()
        }

        override fun onUsageUpdate(totalTokens: Int) {
            if (totalTokens <= 0) return
            _internalState.update { s ->
                if (s.tokenUsage?.totalTokens == totalTokens) s
                else s.copy(tokenUsage = TokenUsageInfo(totalTokens = totalTokens))
            }
        }

        override fun contains(id: String): Boolean = _internalState.value.messages.any { it.id == id }
        override fun indexOf(id: String): Int = _internalState.value.messages.indexOfFirst { it.id == id }
        override fun find(id: String): ChatMessage? = _internalState.value.messages.find { it.id == id }
    }

    init {
        swapChatHandler(protocol.value)
        viewModelScope.launch {
            protocol.drop(1).collect { newProtocol ->
                swapChatHandler(newProtocol)
                if (!skipNextProtocolReset) resetChatState()
                skipNextProtocolReset = false
            }
        }
    }

    private fun resetChatState() {
        _internalState.update { InternalChatState(healthOk = it.healthOk) }
        chatHandler?.clear()
    }

    private fun swapChatHandler(target: ChatProtocol) {
        chatHandler = when (target) {
            ChatProtocol.CHAT_COMPLETIONS ->
                chatCompletionsHandler ?: ChatCompletionsHandler(hermesRepository, messageBridge, messageBridge)
                    .also { chatCompletionsHandler = it }
            ChatProtocol.RUN ->
                chatRunsHandler ?: ChatRunsHandler(hermesRepository, messageBridge, messageBridge)
                    .also { chatRunsHandler = it }
        }
    }

    private val _sessions = MutableStateFlow<List<SessionSummary>>(emptyList())
    val sessions: StateFlow<List<SessionSummary>> = _sessions.asStateFlow()

    fun refreshSessions() {
        if (!hermesRepository.isConfigured.value) return
        viewModelScope.launch(ioDispatcher) {
            _sessions.value = chatHistoryRepository.getSessions().first()
        }
    }

    fun newChat() {
        if (_internalState.value.messages.isEmpty()) return
        streamJob?.cancel()
        resetChatState()
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            inferProtocolFromSessionId(sessionId)?.let { target ->
                if (protocol.value != target) {
                    swapChatHandler(target)
                    skipNextProtocolReset = true
                    localDataStore.setChatProtocol(target.name)
                }
            }
            streamJob?.cancel()
            val messages = chatHistoryRepository.loadSession(sessionId).first()
            _internalState.update { 
                InternalChatState(messages = messages, healthOk = it.healthOk) 
            }
            chatHandler?.clear()
            chatHandler?.setCurrentServerId(sessionId)
        }
    }

    private fun inferProtocolFromSessionId(sessionId: String): ChatProtocol? = when {
        sessionId.startsWith("run_") -> ChatProtocol.RUN
        else -> ChatProtocol.CHAT_COMPLETIONS
    }

    private fun persistCurrentConversation() {
        val sessionId = chatHandler?.currentServerId() ?: return
        val snapshot = _internalState.value.messages
        if (snapshot.isEmpty()) return
        viewModelScope.launch {
            chatHistoryRepository.upsertAll(sessionId, snapshot)
        }
    }

    fun checkHealth() {
        if (!hermesRepository.isConfigured.value) return
        healthJob?.cancel()
        healthJob = viewModelScope.launch(ioDispatcher) {
            hermesRepository.checkHealth().collect { result ->
                when (result) {
                    is HermesResponse.Success -> _internalState.update { it.copy(healthOk = true) }
                    is HermesResponse.Error -> _internalState.update { it.copy(healthOk = false) }
                    else -> Unit
                }
            }
        }
    }

    fun sendChat(text: String, attachment: PendingAttachment? = null) {
        val type = ChatMessageType.determine(text, attachment?.mimeType)
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            type = type,
            text = text,
            imageUri = attachment?.uri,
            mimeType = attachment?.mimeType,
            fileName = attachment?.fileName
        )
        
        _internalState.update { s ->
            s.copy(
                messages = s.messages + userMessage,
                hasPendingRun = true,
                showTypingIndicator = true
            )
        }
        
        streamJob?.cancel()
        streamJob = viewModelScope.launch(ioDispatcher) {
            chatHandler?.sendChat(text, attachment)
        }
    }

    private fun appendErrorMessage(event: SseEvent.Failure) {
        val errorMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.ASSISTANT,
            type = ChatMessageType.Error,
            text = buildFailureText(event),
        )
        _internalState.update { it.copy(messages = it.messages + errorMessage) }
        persistCurrentConversation()
    }

    private fun buildFailureText(event: SseEvent.Failure): String {
        val statusLine = event.response?.let { "HTTP ${it.code} ${it.message}".trim() }
        val cause = event.throwable?.message?.takeIf { it.isNotBlank() }
            ?: event.throwable?.javaClass?.simpleName
        return listOfNotNull(statusLine, cause)
            .joinToString(separator = " · ")
            .ifBlank { "Stream connection failed" }
    }
}
