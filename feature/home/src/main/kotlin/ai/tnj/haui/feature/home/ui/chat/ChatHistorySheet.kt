package ai.tnj.haui.feature.home.ui.chat

import ai.tnj.haui.core.data.db.dao.SessionSummary
import ai.tnj.haui.core.ui.terminalCornerBorders
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistorySheet(
    sessions: List<SessionSummary>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val grouped = remember(sessions) { groupByDay(sessions) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.large,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = {
            val primaryColor = MaterialTheme.colorScheme.primary
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top accent line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(primaryColor.copy(alpha = 0.3f))
                )
                
                // Handle
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .background(primaryColor.copy(alpha = 0.2f), MaterialTheme.shapes.large)
                )

                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "CHAT HISTORY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.em,
                            shadow = Shadow(
                                color = primaryColor.copy(alpha = 0.2f),
                                blurRadius = 12f
                            )
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    thickness = 1.dp,
                    color = primaryColor.copy(alpha = 0.1f)
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
        ) {
            if (sessions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    grouped.forEach { (dayLabel, items) ->
                        item(key = "header-$dayLabel") {
                            DateDivider(label = dayLabel)
                        }
                        items(items.size, key = { idx -> items[idx].sessionId }) { idx ->
                            SessionCard(
                                session = items[idx],
                                onClick = { onSelect(items[idx].sessionId) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.1.em),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun SessionCard(
    session: SessionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val title = remember(session) { sessionTitle(session) }
    val preview = remember(session) { sessionPreview(session) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f))
            .terminalCornerBorders(color = primaryColor.copy(alpha = 0.2f), length = 6.dp)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (preview.isNotBlank()) {
            Text(
                text = preview,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FooterChip(
                    icon = Icons.Outlined.Schedule,
                    text = formatTime(session.lastAt),
                )
                FooterChip(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    text = "${session.messageCount} MSG",
                )
            }
            
            Text(
                text = "ID: ${session.sessionId.take(6).uppercase()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FooterChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "NO_SESSIONS_FOUND",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.2.em),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

private fun sessionTitle(session: SessionSummary): String {
    val firstUser = session.firstUserText?.trim().orEmpty()
    if (firstUser.isNotEmpty()) return firstUser.lineSequence().first()
    return "SESSION_ALPHA_${session.sessionId.take(4).uppercase()}"
}

private fun sessionPreview(session: SessionSummary): String {
    val firstAssistant = session.firstAssistantText?.trim().orEmpty()
    if (firstAssistant.isNotEmpty()) return firstAssistant.replace('\n', ' ')
    val firstUser = session.firstUserText?.trim().orEmpty()
    if (firstUser.isNotEmpty()) return firstUser.replace('\n', ' ')
    return "No content available."
}

private val timeFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("HH:mm:ss", Locale.getDefault())
}

private fun formatTime(epochMillis: Long): String =
    timeFormatter.format(Date(epochMillis))

private fun groupByDay(sessions: List<SessionSummary>): List<Pair<String, List<SessionSummary>>> {
    if (sessions.isEmpty()) return emptyList()
    val cal = Calendar.getInstance()
    val ordered = LinkedHashMap<String, MutableList<SessionSummary>>()
    
    sessions.forEach { session ->
        cal.timeInMillis = session.lastAt
        val key = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        ordered.getOrPut(key) { mutableListOf() }.add(session)
    }
    return ordered.map { (k, v) -> k to v }
}
