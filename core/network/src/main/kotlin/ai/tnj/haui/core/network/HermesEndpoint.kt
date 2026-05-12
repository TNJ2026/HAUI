package ai.tnj.haui.core.network

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Snapshot of the active Hermes target.
 */
data class HermesEndpointState(
    val host: String = "",
    val port: String = "",
    val apiKey: String = "",
) {
    /**
     * Endpoint is "configured" only when host is non-blank AND port parses to a
     * valid TCP port (1..65535). Validating here means [HermesEndpointInterceptor]
     * can call `port.toInt()` without risk of a NumberFormatException at send time.
     */
    val isConfigured: Boolean
        get() = host.isNotBlank() && (port.toIntOrNull() in 1..65535)
}

/**
 * Mutable holder for the active Hermes server target. Retrofit is configured
 * once with [PLACEHOLDER_BASE_URL]; the interceptor below rewrites every
 * request's host:port at send time, and injects the bearer token if present.
 *
 * Direct OkHttp requests built by `HermesRequests` (currently used for SSE)
 * already embed the real host, so they pass through the interceptor untouched.
 */
@Singleton
class HermesEndpoint @Inject constructor() {

    private val _state = MutableStateFlow(HermesEndpointState())

    /** Full endpoint snapshot, emits on every [update]. */
    val state: StateFlow<HermesEndpointState> = _state.asStateFlow()

    /** Convenience flow that flips to `true` once host & port are non-blank. */
    val isConfigured: StateFlow<Boolean> = state.mapState { it.isConfigured }

    val host: String get() = _state.value.host
    val port: String get() = _state.value.port
    val apiKey: String get() = _state.value.apiKey

    fun update(host: String, port: String, apiKey: String) {
        _state.value = HermesEndpointState(host.trim(), port.trim(), apiKey.trim())
    }

    companion object {
        /** Sentinel host used as Retrofit base URL; rewritten by the interceptor. */
        const val PLACEHOLDER_HOST = "hermes.placeholder"
        const val PLACEHOLDER_BASE_URL = "http://$PLACEHOLDER_HOST/"
    }
}

/**
 * Returns a [StateFlow] view that projects each upstream value through [transform].
 * Reads are O(1) (delegates to upstream `.value`); collectors see distinct values only.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> {
    val source = this
    return object : StateFlow<R> {
        override val value: R get() = transform(source.value)
        override val replayCache: List<R> get() = listOf(value)
        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            source.map(transform).distinctUntilChanged().collect { collector.emit(it) }
            error("StateFlow upstream never completes")
        }
    }
}

@Singleton
class HermesEndpointInterceptor @Inject constructor(
    private val endpoint: HermesEndpoint,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.url.host == HermesEndpoint.PLACEHOLDER_HOST) {
            val snapshot = endpoint.state.value
            check(snapshot.isConfigured) {
                "HermesEndpoint not configured; call HermesRepository.updateBaseUrl(...) first."
            }
            val portInt = snapshot.port.toIntOrNull()
                ?: error("HermesEndpoint has invalid port '${snapshot.port}' — should have been rejected by isConfigured")
            val newUrl = request.url.newBuilder()
                .scheme("http")
                .host(snapshot.host)
                .port(portInt)
                .build()
            val builder = request.newBuilder().url(newUrl)
            if (snapshot.apiKey.isNotBlank() && request.header("Authorization") == null) {
                builder.addHeader("Authorization", "Bearer ${snapshot.apiKey}")
            }
            request = builder.build()
        }

        return chain.proceed(request)
    }
}
