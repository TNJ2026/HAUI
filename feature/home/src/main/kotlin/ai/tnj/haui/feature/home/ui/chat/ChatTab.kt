package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.data.chat.ChatUiState
import ai.tnj.haui.core.data.db.dao.SessionSummary
import ai.tnj.haui.core.utils.PendingAttachment
import ai.tnj.haui.core.utils.loadDocumentAttachment
import ai.tnj.haui.core.utils.loadSizedImageAttachment
import ai.tnj.haui.feature.home.ui.settings.ChatProtocol
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChatTab(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val protocol by viewModel.protocol.collectAsStateWithLifecycle()
    val showToolBubble by viewModel.showToolBubble.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()

    ChatTabContent(
        uiState = uiState,
        showAttachmentButton = protocol == ChatProtocol.CHAT_COMPLETIONS,
        showToolBubble = showToolBubble,
        sessions = sessions,
        onNewChat = viewModel::newChat,
        onSend = viewModel::sendChat,
        onLoadSession = viewModel::loadSession,
        onRefreshSessions = viewModel::refreshSessions,
        modifier = modifier
    )
}

@Composable
private fun ChatTabContent(
    uiState: ChatUiState,
    showAttachmentButton: Boolean,
    showToolBubble: Boolean,
    sessions: List<SessionSummary>,
    onNewChat: () -> Unit,
    onSend: (String, PendingAttachment?) -> Unit,
    onLoadSession: (String) -> Unit,
    onRefreshSessions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val attachments = remember { mutableStateListOf<PendingAttachment>() }
    var showHistorySheet by remember { mutableStateOf(false) }

    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val next = try { loadSizedImageAttachment(context.contentResolver, it) } catch (_: Throwable) { null }
                next?.let { withContext(Dispatchers.Main) { attachments.add(it) } }
            }
        }
    }

    val pickDocuments = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                val next = try { loadDocumentAttachment(context.contentResolver, it) } catch (_: Throwable) { null }
                next?.let { withContext(Dispatchers.Main) { attachments.add(it) } }
            }
        }
    }

    val messages = remember(uiState) { (uiState as? ChatUiState.ChatUIData)?.messages.orEmpty() }
    val showNewChatAction = messages.isNotEmpty()

    val openHistory: () -> Unit = remember {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            showHistorySheet = true
        }
    }

    Column(
        modifier = modifier.fillMaxSize().imePadding()
    ) {
        ChatAppBar(
            onNewChat = onNewChat,
            onHistory = openHistory,
            showNewChat = showNewChatAction,
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            val data = uiState as? ChatUiState.ChatUIData
            
            Box(modifier = Modifier.weight(1f)) {
                ChatMessageListCard(
                    messages = messages,
                    healthOk = data?.healthOk ?: true,
                    onOpenHistory = openHistory,
                    showTypingIndicator = data?.showTypingIndicator ?: false,
                    showToolBubble = showToolBubble,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }

            ChatComposer(
                healthOk = data?.healthOk ?: true,
                hasPendingRun = data?.hasPendingRun ?: false,
                attachments = attachments,
                contextProgress = data?.tokenUsage?.progress ?: 0f,
                showAttachmentButton = showAttachmentButton,
                onPickImages = { pickImages.launch("image/*") },
                onPickDocuments = {
                    pickDocuments.launch(
                        arrayOf(
                            "text/*", "application/pdf", "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                },
                onRemoveAttachment = { id -> attachments.removeAll { it.id == id } },
                onSend = { text ->
                    onSend(text, attachments.firstOrNull())
                    attachments.clear()
                }
            )
        }
    }

    if (showHistorySheet) {
        LaunchedEffect(Unit) { onRefreshSessions() }
        ChatHistorySheet(
            sessions = sessions,
            onSelect = { sessionId ->
                onLoadSession(sessionId)
                showHistorySheet = false
            },
            onDismiss = { showHistorySheet = false },
        )
    }
}

@Composable
private fun ChatAppBar(
    onNewChat: () -> Unit,
    onHistory: () -> Unit,
    showNewChat: Boolean,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHAT",
                color = primaryColor,
                style = MaterialTheme.typography.headlineLarge.copy(
                    letterSpacing = 0.1.em,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppBarAction(
                    icon = Icons.Outlined.History,
                    contentDescription = "History",
                    onClick = onHistory
                )

                if (showNewChat) {
                    AppBarAction(
                        icon = Icons.Outlined.AddComment,
                        contentDescription = "New Chat",
                        onClick = onNewChat
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = primaryColor.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun AppBarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
    ) {
        Box(modifier = Modifier.size(width = 44.dp, height = 36.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
