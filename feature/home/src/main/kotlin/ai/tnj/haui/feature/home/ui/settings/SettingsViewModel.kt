package ai.tnj.haui.feature.home.ui.settings

import ai.tnj.haui.core.data.LocalDataStore
import ai.tnj.haui.core.data.di.IoDispatcher
import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.designsystem.ThemeController
import ai.tnj.haui.core.model.HermesJob
import ai.tnj.haui.core.model.HermesResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val localDataStore: LocalDataStore,
    private val hermesRepository: HermesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(capacity = Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        // Sync UI state with DataStore preferences using combine for efficiency
        combine(
            localDataStore.isDarkTheme,
            localDataStore.chatProtocol,
            localDataStore.showToolBubble
        ) { isDark, protocolRaw, showTool ->
            SettingsUiState(
                themeMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT,
                protocol = parseChatProtocol(protocolRaw),
                showToolBubble = showTool
            )
        }
        .distinctUntilChanged()
        .onEach { newState -> 
            _uiState.update { it.copy(
                themeMode = newState.themeMode,
                protocol = newState.protocol,
                showToolBubble = newState.showToolBubble
            ) }
        }
        .launchIn(viewModelScope)

        // Auto-fetch jobs when connected
        hermesRepository.isConfigured
            .filter { it }
            .onEach { refreshJobs() }
            .launchIn(viewModelScope)
    }

    fun refreshJobs() {
        if (!hermesRepository.isConfigured.value) return
        viewModelScope.launch(ioDispatcher) {
            hermesRepository.jobs().collect { result ->
                when (result) {
                    is HermesResponse.Loading -> {
                        _uiState.update { it.copy(isJobsLoading = true) }
                    }
                    is HermesResponse.Success -> {
                        _uiState.update {
                            it.copy(
                                isJobsLoading = false,
                                jobsError = null,
                                jobs = result.data.jobs.map(::toJobItem),
                            )
                        }
                    }
                    is HermesResponse.Error -> {
                        _uiState.update {
                            it.copy(
                                isJobsLoading = false,
                                jobsError = "${result.exception.code}: ${result.exception.msg}",
                            )
                        }
                    }
                }
            }
        }
    }

    fun togglePause(job: JobItem) {
        val pausing = job.enabled
        val flow = if (pausing) hermesRepository.pauseJob(job.id) else hermesRepository.resumeJob(job.id)
        val action = if (pausing) "Pause" else "Resume"
        
        runJobAction(
            flow = flow,
            description = "$action '${job.name}'",
            onSuccess = { applyPauseLocally(job.id, paused = pausing) }
        )
    }

    fun runJobNow(job: JobItem) {
        runJobAction(
            flow = hermesRepository.runJob(job.id),
            description = "Run '${job.name}'",
            onSuccess = { refreshJobs() }
        )
    }

    fun deleteJob(job: JobItem) {
        runJobAction(
            flow = hermesRepository.deleteJob(job.id),
            description = "Delete '${job.name}'",
            onSuccess = { refreshJobs() }
        )
    }

    private fun applyPauseLocally(jobId: String, paused: Boolean) {
        val newStatus = if (paused) JobStatus.PAUSED else JobStatus.SCHEDULED
        _uiState.update { state ->
            state.copy(
                jobs = state.jobs.map { item ->
                    if (item.id != jobId) item else item.copy(
                        enabled = !paused,
                        status = newStatus,
                        statusLabel = newStatus.name
                    )
                }
            )
        }
    }

    private fun runJobAction(
        flow: Flow<HermesResponse<Unit>>,
        description: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch(ioDispatcher) {
            flow.collect { result ->
                when (result) {
                    is HermesResponse.Success -> {
                        _messages.trySend("$description succeeded")
                        onSuccess()
                    }
                    is HermesResponse.Error -> {
                        _messages.trySend("$description failed: ${result.exception.msg}")
                    }
                    else -> Unit
                }
            }
        }
    }

    fun selectTheme(mode: ThemeMode) {
        val isDark = mode == ThemeMode.DARK
        ThemeController.setDark(isDark)
        viewModelScope.launch(ioDispatcher) {
            localDataStore.setDarkTheme(isDark)
        }
    }

    fun selectProtocol(protocol: ChatProtocol) {
        viewModelScope.launch(ioDispatcher) {
            localDataStore.setChatProtocol(protocol.name)
        }
    }

    fun setShowToolBubble(show: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            localDataStore.setShowToolBubble(show)
        }
    }

    private fun toJobItem(job: HermesJob): JobItem {
        val displayName = job.name.ifBlank { job.id.ifBlank { "untitled" } }
        val cronExpr = job.scheduleDisplay
            .ifBlank { job.schedule?.display.orEmpty() }
            .ifBlank { job.schedule?.expr.orEmpty() }
        val status = parseJobStatus(job)
        return JobItem(
            id = job.id,
            name = displayName,
            description = job.prompt,
            status = status,
            statusLabel = statusLabel(job, status),
            cronExpr = cronExpr,
            lastRunAt = formatRunAt(job.lastRunAt),
            nextRunAt = formatRunAt(job.nextRunAt),
            enabled = job.enabled,
        )
    }

    private fun parseJobStatus(job: HermesJob): JobStatus {
        if (!job.enabled) return JobStatus.PAUSED
        return when (job.state.trim().lowercase()) {
            "failed", "error", "failure" -> JobStatus.FAILED
            "running", "active", "in_progress" -> JobStatus.RUNNING
            "paused" -> JobStatus.PAUSED
            "scheduled" -> JobStatus.SCHEDULED
            "queued", "pending", "waiting" -> JobStatus.QUEUED
            "completed", "succeeded", "success", "done", "finished" -> JobStatus.COMPLETED
            else -> when (job.lastStatus?.trim()?.lowercase()) {
                "failed", "error", "failure" -> JobStatus.FAILED
                "completed", "succeeded", "success", "done" -> JobStatus.COMPLETED
                else -> JobStatus.QUEUED
            }
        }
    }

    private fun statusLabel(job: HermesJob, status: JobStatus): String {
        val raw = job.state.trim()
        return if (raw.isNotBlank()) raw.uppercase() else status.name
    }

    private fun formatRunAt(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        val tIdx = raw.indexOf('T')
        if (tIdx < 0) return raw
        val datePart = raw.substring(0, tIdx)
        val timePart = raw.substring(tIdx + 1).substringBefore('.').substringBefore('+').substringBefore('Z').take(8)
        val dateFmt = datePart.split('-').takeIf { it.size == 3 }?.let { (y, m, d) -> 
            "$y/${m.toIntOrNull() ?: m}/${d.toIntOrNull() ?: d}" 
        } ?: datePart
        return "$dateFmt $timePart".trim()
    }
}
