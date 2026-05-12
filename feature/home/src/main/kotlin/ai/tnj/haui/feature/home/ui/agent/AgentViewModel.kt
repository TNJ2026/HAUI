package ai.tnj.haui.feature.home.ui.agent

import ai.tnj.haui.core.data.LocalDataStore
import ai.tnj.haui.core.data.repository.HermesRepository
import ai.tnj.haui.core.data.di.IoDispatcher
import ai.tnj.haui.core.model.HermesModelData
import ai.tnj.haui.core.model.HermesResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentUiState(
    val isConnected: Boolean = false,
    val health: String = "Unknown",
    val isConnecting: Boolean = false,
    val connectError: String? = null,
    val gatewayState: String = "Unknown",
    val connection: String = "Unconnected",
    /** API server's last `updated_at` timestamp; empty when unknown. */
    val uptime: String = "",
    val host: String = "",
    val port: String = "",
    val apiKey: String = "",
    val models: List<HermesModelData> = emptyList(),
    val isDarkTheme: Boolean = true
)

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val hermesRepository: HermesRepository,
    private val localDataStore: LocalDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    private var lastHealthCheckAtMs: Long = 0L

    init {
        viewModelScope.launch {
            val (savedHost, savedPort, savedApiKey) = localDataStore.getServerConfig()
            
            // Surface saved config so the ConnectionSheet can pre-fill its
            // inputs even before any successful connect runs.
            _uiState.update {
                it.copy(host = savedHost, port = savedPort, apiKey = savedApiKey)
            }
            if (savedHost.isNotEmpty() && savedPort.isNotEmpty()) {
                if (!_uiState.value.isConnected && !_uiState.value.isConnecting) {
                    connect(savedHost, savedPort, savedApiKey, true)
                }
            }
        }
        viewModelScope.launch {
            localDataStore.isDarkTheme.collect {
                _uiState.update { state -> state.copy(isDarkTheme = it) }
            }
        }
    }

    fun connect(
        host: String,
        port: String,
        apiKey: String,
        useCachedValue: Boolean = false
    ) {
        _uiState.update { it.copy(isConnecting = true, connectError = null) }
        hermesRepository.updateBaseUrl(host, port, apiKey)
        fetchModels(host, port, apiKey, useCachedValue)
    }

    fun clearConnectError() {
        _uiState.update { it.copy(connectError = null) }
    }

    /**
     * Re-runs the health check using the currently active host/port. Intended
     * to be called from `ON_RESUME` so the connection card reflects the latest
     * server status whenever the user returns to the tab.
     */
    fun refreshHealth() {
        if (!hermesRepository.isConfigured.value) return
        val current = _uiState.value
        if (current.host.isBlank() || current.port.isBlank()) return
        val now = System.currentTimeMillis()
        if (now - lastHealthCheckAtMs < HEALTH_CHECK_THROTTLE_MS) return
        lastHealthCheckAtMs = now
        checkHealth(current.host, current.port)
    }

    private fun fetchModels(
        host: String,
        port: String,
        apiKey: String,
        useCachedValue: Boolean = false
    ) {
        viewModelScope.launch(ioDispatcher) {
            hermesRepository.getModels().collect { result ->
                when (result) {
                    is HermesResponse.Success -> {
                        if (!useCachedValue) localDataStore.saveServerConfig(host, port, apiKey)
                        _uiState.update {
                            it.copy(
                                models = result.data.data,
                                host = host,
                                port = port,
                                apiKey = apiKey,
                            )
                        }
                        checkHealth(host, port)
                    }
                    is HermesResponse.Error -> {
                        val msg = if (result.exception.code == 401) {
                            if (apiKey.isBlank()) {
                                "Unauthorized, Please input API key"
                            } else {
                                "Unauthorized, incorrect API key"
                            }
                        } else {
                            "request error:${result.exception.code}, ${result.exception.msg}"
                        }
                        _uiState.update {
                            it.copy(isConnecting = false, connectError = msg)
                        }
                    }
                    is HermesResponse.Loading -> {}
                }
            }
        }
    }

    private fun checkHealth(
        host: String,
        port: String
    ) {
        viewModelScope.launch(ioDispatcher) {
            hermesRepository.checkHealth().collect { result ->
                when (result) {
                    is HermesResponse.Success -> {
                        _uiState.update {
                            it.copy(
                                isConnecting = false,
                                isConnected = true,
                                health = result.data.status,
                                connection = result.data.platforms.apiServer.state,
                                gatewayState = result.data.gatewayState,
                                uptime = formatUptime(result.data.platforms.apiServer.updatedAt),
                                host = host,
                                port = port,
                            )
                        }
                    }
                    is HermesResponse.Error -> {
                        _uiState.update {
                            it.copy(
                                isConnecting = false,
                                isConnected = false,
                                connection = "Unconnected",
                                gatewayState = "Unknown",
                                uptime = "",
                                connectError = "Health check failed",
                            )
                        }
                    }
                    is HermesResponse.Loading -> {}
                }
            }
        }
    }

    private companion object {
        const val HEALTH_CHECK_THROTTLE_MS = 30_000L
    }

}

/**
 * Trims an ISO-8601 timestamp (e.g. `2026-05-05T08:45:12.123+08:00`) into a
 * compact `YYYY/M/D HH:mm:ss` form for the UPTIME readout. Returns the raw
 * value if it doesn't look like ISO-8601.
 */
private fun formatUptime(raw: String): String {
    if (raw.isBlank()) return ""
    val tIdx = raw.indexOf('T')
    if (tIdx < 0) return raw
    val datePart = raw.substring(0, tIdx)
    val time = raw.substring(tIdx + 1)
        .substringBefore('.')
        .substringBefore('+')
        .substringBefore('Z')
        .take(8)
    val date = datePart.split('-')
        .takeIf { it.size == 3 }
        ?.let { (y, m, d) -> "$y/${m.toIntOrNull() ?: m}/${d.toIntOrNull() ?: d}" }
        ?: datePart
    return "$date $time".trim()
}
