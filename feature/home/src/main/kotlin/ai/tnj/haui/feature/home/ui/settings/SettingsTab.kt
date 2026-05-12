package ai.tnj.haui.feature.home.ui.settings

import ai.tnj.haui.core.ui.terminalCornerBorders
import ai.tnj.haui.feature.home.ui.components.PanelHeader
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class SettingsActions(
    val onSelectTheme: (ThemeMode) -> Unit,
    val onSelectProtocol: (ChatProtocol) -> Unit,
    val onToggleToolBubble: (Boolean) -> Unit,
    val onTogglePause: (JobItem) -> Unit,
    val onRunJob: (JobItem) -> Unit,
    val onDeleteJob: (JobItem) -> Unit,
)

@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshJobs()
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val actions = remember(viewModel) {
        SettingsActions(
            onSelectTheme = viewModel::selectTheme,
            onSelectProtocol = viewModel::selectProtocol,
            onToggleToolBubble = viewModel::setShowToolBubble,
            onTogglePause = viewModel::togglePause,
            onRunJob = viewModel::runJobNow,
            onDeleteJob = viewModel::deleteJob,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsAppBar()
            SettingsTabList(
                state = uiState,
                actions = actions,
                modifier = Modifier.weight(1f)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RectangleShape,
            )
        }
    }
}

@Composable
private fun SettingsTabList(
    state: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SystemThemePanel(state.themeMode, actions.onSelectTheme)
        ProtocolSwitchPanel(state.protocol, actions.onSelectProtocol)
        ChatDisplayPanel(state.showToolBubble, actions.onToggleToolBubble)
        ActiveJobsPanel(state, actions)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsAppBar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "SETTINGS",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge
            )
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun SystemThemePanel(mode: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    TerminalPanel(title = "[ SYSTEM_THEME ]") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ThemeCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.LightMode,
                label = "LIGHT_MODE",
                selected = mode == ThemeMode.LIGHT,
                onClick = { onSelect(ThemeMode.LIGHT) },
            )
            ThemeCell(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.DarkMode,
                label = "DARK_MODE",
                selected = mode == ThemeMode.DARK,
                onClick = { onSelect(ThemeMode.DARK) },
            )
        }
    }
}

@Composable
private fun ThemeCell(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderColor = if (selected) tint.copy(alpha = 0.4f) else tint.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .border(1.dp, borderColor)
            .background(if (selected) tint.copy(alpha = 0.1f) else Color.Transparent)
            .then(if (selected) Modifier.terminalCornerBorders(tint) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Text(
                text = label,
                color = tint,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
private fun ProtocolSwitchPanel(protocol: ChatProtocol, onSelect: (ChatProtocol) -> Unit) {
    TerminalPanel(title = "[ PROTOCOL_SWITCH ]") {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ChatProtocol.entries.forEach { item ->
                ProtocolOptionRow(
                    protocol = item,
                    selected = protocol == item,
                    onClick = { onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun ProtocolOptionRow(protocol: ChatProtocol, selected: Boolean, onClick: () -> Unit) {
    val tint = MaterialTheme.colorScheme.primary
    val borderColor = if (selected) tint else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor)
            .background(if (selected) tint.copy(alpha = 0.05f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RadioMark(selected)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = protocol.displayName,
                        color = if (selected) tint else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        ),
                    )
                    if (selected) Box(Modifier.size(6.dp).background(tint, CircleShape))
                }
                Text(
                    text = protocol.description,
                    color = if (selected) tint.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (selected) Text("]", color = tint.copy(alpha = 0.2f), style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace))
        }
    }
}

@Composable
private fun RadioMark(selected: Boolean) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(16.dp)
            .border(1.dp, tint, CircleShape)
            .background(if (selected) tint.copy(alpha = 0.2f) else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(8.dp).background(tint, CircleShape))
    }
}

@Composable
private fun ChatDisplayPanel(show: Boolean, onToggle: (Boolean) -> Unit) {
    TerminalPanel(title = "[ CHAT_DISPLAY ]") {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onToggle(!show) }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TOOL_BUBBLE",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "Show tool-call bubbles inline in the chat stream.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            SquareSwitch(checked = show, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SquareSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val trackWidth = 44.dp
    val thumbSize = 16.dp
    val activeColor = MaterialTheme.colorScheme.primary
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - 2.dp else 2.dp,
        animationSpec = tween(120),
        label = "thumb_offset",
    )

    Box(
        modifier = Modifier
            .size(trackWidth, 22.dp)
            .background(if (checked) activeColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (checked) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.padding(start = thumbOffset).size(thumbSize).background(if (checked) activeColor else MaterialTheme.colorScheme.outline))
    }
}

@Composable
private fun ActiveJobsPanel(state: SettingsUiState, actions: SettingsActions) {
    TerminalPanel(title = "[ JOBS ]") {
        when {
            state.isJobsLoading && state.jobs.isEmpty() -> JobsPlaceholder("loading…")
            state.jobsError != null && state.jobs.isEmpty() -> JobsPlaceholder(state.jobsError, true)
            state.jobs.isEmpty() -> JobsPlaceholder("no scheduled tasks")
            else -> Column(Modifier.fillMaxWidth()) {
                state.jobs.forEachIndexed { index, job ->
                    JobCard(
                        job = job,
                        isLast = index == state.jobs.lastIndex,
                        onTogglePause = { actions.onTogglePause(job) },
                        onRunNow = { actions.onRunJob(job) },
                        onDelete = { actions.onDeleteJob(job) },
                    )
                }
            }
        }
    }
}

@Composable
private fun JobsPlaceholder(text: String, isError: Boolean = false) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun JobCard(job: JobItem, isLast: Boolean, onTogglePause: () -> Unit, onRunNow: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(job.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                JobStatusBadge(job.status, job.statusLabel)
            }
            if (job.description.isNotBlank()) Text(job.description, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp), maxLines = 3, overflow = TextOverflow.Ellipsis)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (job.cronExpr.isNotBlank()) Text(job.cronExpr, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                JobMetaText("上次", job.lastRunAt)
                JobMetaText("下次", job.nextRunAt)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            JobIconButton(onTogglePause, if (job.enabled) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (job.enabled) "Pause" else "Resume")
            JobIconButton(onRunNow, Icons.Filled.Bolt, "Run now")
            JobIconButton(onDelete, Icons.Filled.Delete, "Delete")
        }
    }
    if (!isLast) Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
}

@Composable
private fun JobMetaText(label: String, value: String) {
    if (value.isBlank() || value == "—") return
    Text("$label: $value", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp))
}

@Composable
private fun JobIconButton(onClick: () -> Unit, icon: ImageVector, contentDescription: String) {
    IconButton(onClick = onClick, modifier = Modifier.size(32.dp)) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun JobStatusBadge(status: JobStatus, label: String) {
    val style = when (status) {
        JobStatus.FAILED -> StatusStyle(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
        JobStatus.RUNNING, JobStatus.SCHEDULED -> StatusStyle(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        JobStatus.QUEUED -> StatusStyle(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
        JobStatus.PAUSED, JobStatus.COMPLETED -> StatusStyle(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }
    Box(Modifier.background(style.bg).border(1.dp, style.border).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label.ifBlank { status.name }.uppercase(), color = style.fg, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 0.5.sp))
    }
}

private data class StatusStyle(val fg: Color, val bg: Color, val border: Color)

@Composable
private fun TerminalPanel(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        shape = RectangleShape,
    ) {
        Column {
            PanelHeader(title = title)
            content()
        }
    }
}
