package ai.tnj.haui.feature.home.ui.settings

enum class ThemeMode { LIGHT, DARK }

enum class ChatProtocol(val displayName: String, val description: String) {
    CHAT_COMPLETIONS(
        displayName = "ChatCompletions",
        description = "Standard dialogue interface. Low resource drain.",
    ),
    RUN(
        displayName = "Run",
        description = "Advanced execution environment. Direct API tether established.",
    ),
}

enum class JobStatus { FAILED, RUNNING, SCHEDULED, QUEUED, COMPLETED, PAUSED }

data class JobItem(
    val id: String,
    val name: String,
    val description: String,
    val status: JobStatus,
    val statusLabel: String,
    val cronExpr: String,
    val lastRunAt: String,
    val nextRunAt: String,
    val enabled: Boolean,
)

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val protocol: ChatProtocol = ChatProtocol.RUN,
    val showToolBubble: Boolean = false,
    val jobs: List<JobItem> = emptyList(),
    val isJobsLoading: Boolean = false,
    val jobsError: String? = null,
)

internal fun parseChatProtocol(raw: String): ChatProtocol =
    ChatProtocol.entries.firstOrNull { it.name == raw } ?: ChatProtocol.RUN
