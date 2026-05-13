package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.MessageRole
import ai.tnj.haui.core.ui.terminalCornerBorders
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun ChatMessageListCard(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    healthOk: Boolean,
    onOpenHistory: () -> Unit,
    showTypingIndicator: Boolean = false,
    showToolBubble: Boolean = true,
) {
    val listState = rememberLazyListState()
    
    // Ensure we scroll to bottom (index 0 because of reverseLayout) when new messages arrive
    LaunchedEffect(messages.size, showTypingIndicator) {
        if (messages.isNotEmpty() || showTypingIndicator) {
            listState.animateScrollToItem(0)
        }
    }

    val displayMessages = remember(messages, showToolBubble) {
        val visible = if (showToolBubble) messages else messages.filter { it.role != MessageRole.TOOL }
        visible.asReversed()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Footer spacer for the composer overlap
            item(key = "list_footer") {
                Box(Modifier.height(8.dp).fillMaxWidth())
            }

            if (showTypingIndicator) {
                item(key = "typing_indicator") {
                    ChatTypingIndicatorBubble()
                }
            }

            items(
                items = displayMessages,
                key = { it.id },
                contentType = { it.role }
            ) { message ->
                ChatMessageBubble(message = message)
            }
        }

        if (displayMessages.isEmpty()) {
            EmptyChatHint(
                healthOk = healthOk,
                onOpenHistory = onOpenHistory,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun EmptyChatHint(
    healthOk: Boolean,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main status card
        Box(modifier = Modifier.width(IntrinsicSize.Max)) {
            Column(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f))
                    .terminalCornerBorders(color = primaryColor.copy(alpha = 0.3f), length = 12.dp)
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icon box
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RectangleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.1f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = primaryColor.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = "SYSTEM_READY",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        letterSpacing = 0.2.em,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val hintText = if (healthOk) {
                    "SECURE LINE ESTABLISHED. SELECT PREVIOUS SESSION OR INITIALIZE NEW STREAM."
                } else {
                    "GATEWAY_OFFLINE. AUTHENTICATION REQUIRED TO ENABLE SECURE PROTOCOLS."
                }

                Text(
                    text = hintText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        lineHeight = 18.sp,
                        letterSpacing = 0.05.em,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(260.dp)
                )

                // Resume Session Button
                Surface(
                    onClick = onOpenHistory,
                    shape = RectangleShape,
                    color = primaryColor.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = primaryColor
                        )
                        Text(
                            text = "ACCESS_HISTORY_VAULT",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.1.em
                            )
                        )
                    }
                }

                // Status Ref
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                    Text(
                        text = "REF: 0x88-HAUI-A0",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                }
            }

            // System_Status badge
            Box(
                modifier = Modifier
                    .offset(x = 16.dp, y = (-10).dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "CORE_STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.1.em
                    ),
                    color = primaryColor
                )
            }
        }
    }
}
