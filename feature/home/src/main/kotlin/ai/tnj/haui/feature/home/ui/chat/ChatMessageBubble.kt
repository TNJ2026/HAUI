package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.MessageRole
import ai.tnj.haui.core.ui.terminalCornerBorders
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import java.util.Locale

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    if (message.type == ChatMessageType.Error) {
        ChatErrorBubble(message)
        return
    }
    when (message.role) {
        MessageRole.USER -> ChatUserBubble(message)
        MessageRole.TOOL -> ChatToolBubble(message)
        else -> ChatAssistantBubble(message)
    }
}

@Composable
private fun ChatUserBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "USER",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.1.em,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp, end = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            val paddingModifier = remember(message.type) {
                if (message.type == ChatMessageType.Text) {
                    Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                } else {
                    Modifier.padding(2.dp)
                }
            }
            Column(modifier = paddingModifier) {
                ChatMessageBody(message, MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ChatAssistantBubble(message: ChatMessage) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(14.dp)
            )
            Box {
                Text(
                    text = "HERMES AI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.1.em,
                        shadow = Shadow(color = primaryColor.copy(alpha = 0.3f), blurRadius = 8f)
                    ),
                    color = primaryColor
                )
            }
        }
        
        Surface(
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = RectangleShape, clip = false)
                .terminalCornerBorders(color = primaryColor.copy(alpha = 0.3f), length = 8.dp),
            shape = RectangleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                ChatMessageBody(message, MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ChatToolBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Construction,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "SYSTEM_TOOL_EXECUTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.05.em,
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }

        Surface(
            shape = RectangleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                ChatMessageBody(message, MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ChatMessageBody(msg: ChatMessage, textColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (msg.type) {
            ChatMessageType.Text -> {
                val text = msg.text
                val isTerminalOutput = remember(msg.role, text) {
                    msg.role == MessageRole.ASSISTANT && text.trimStart().startsWith(">")
                }

                if (isTerminalOutput) {
                    val lines = remember(text) { text.split("\n") }
                    Column {
                        Text(
                            text = lines[0],
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (lines.size > 1) {
                            ChatMarkdown(
                                text = remember(lines) { lines.drop(1).joinToString("\n") },
                                textColor = textColor
                            )
                        }
                    }
                } else {
                    ChatMarkdown(text = text, textColor = textColor)
                }
            }
            ChatMessageType.Image -> {
                val uri = msg.imageUri ?: return@Column
                ChatImageContent(uri)
            }
            ChatMessageType.Document -> {
                val fileName = msg.fileName ?: return@Column
                ChatDocumentContent(fileName)
            }
            ChatMessageType.Combination -> {
                if (msg.mimeType?.startsWith("image/") == true) {
                    msg.imageUri?.let { ChatImageContent(it, msg.text) }
                } else {
                    msg.fileName?.let { ChatDocumentContent(it, msg.text) }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ChatImageContent(uri: Uri, input: String? = null) {
    Column(modifier = Modifier.width(220.dp).padding(4.dp)) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = uri,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
        }
        if (!input.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = input,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ChatDocumentContent(fileName: String, input: String? = null) {
    Column(modifier = Modifier.width(220.dp).padding(4.dp)) {
        Surface(
            shape = RoundedCornerShape(2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!input.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = input,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ChatTypingIndicatorBubble() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(
            shape = RectangleShape,
            modifier = Modifier.terminalCornerBorders(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                DotPulse(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ChatErrorBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "!! SYSTEM_FAILURE_DETECTED",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.05.em,
            ),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            shape = RectangleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun DotPulse(color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            PulseDotWithDelay(color = color, delay = index * 300)
        }
    }
}

@Composable
private fun PulseDotWithDelay(color: Color, delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(delay)
        ),
        label = "alpha"
    )
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun ChatCodeBlock(code: String, language: String?) {
    Surface(
        shape = RectangleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!language.isNullOrBlank()) {
                Text(
                    text = "SOURCE_CODE: ${language.uppercase(Locale.US)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.1.em,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
            }
            Text(
                text = code.trimEnd(),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
