package ai.tnj.haui.core.data.repository

import ai.tnj.haui.core.data.HermesRequests
import ai.tnj.haui.core.model.HermesHealth
import ai.tnj.haui.core.model.HermesJobs
import ai.tnj.haui.core.model.HermesModels
import ai.tnj.haui.core.model.HermesResponse
import ai.tnj.haui.core.model.HermesRun
import ai.tnj.haui.core.model.HermesRunState
import ai.tnj.haui.core.model.HermesRunStopModel
import ai.tnj.haui.core.model.OpenAIResponsesRequestBody
import ai.tnj.haui.core.model.RunRequestBody
import ai.tnj.haui.core.network.HermesEndpoint
import ai.tnj.haui.core.network.HermesService
import ai.tnj.haui.core.network.NetworkManager
import ai.tnj.haui.core.network.SseEvent
import ai.tnj.haui.core.network.hermesFlow
import ai.tnj.haui.core.utils.PendingAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Hermes network operations.
 *
 *  - Non-streaming endpoints expose `Flow<HermesResponse<T>>` so callers can
 *    react to `Loading` / `Success` / `Error` uniformly.
 *  - Streaming endpoints continue to expose raw `Flow<SseEvent>`.
 */
interface HermesRepository {

    /** Emits `true` once host & port have been configured via [updateBaseUrl]. */
    val isConfigured: StateFlow<Boolean>

    fun updateBaseUrl(host: String, port: String, apiKey: String = "")

    fun checkHealth(): Flow<HermesResponse<HermesHealth>>

    fun getModels(): Flow<HermesResponse<HermesModels>>

    fun runs(input: String, runId: String?): Flow<HermesResponse<HermesRun>>

    fun runsState(runId: String): Flow<HermesResponse<HermesRunState>>

    fun stopRun(runId: String): Flow<HermesResponse<HermesRunStopModel>>

    fun chatResponse(input: String): Flow<HermesResponse<HermesRun>>

    fun jobs(): Flow<HermesResponse<HermesJobs>>

    fun pauseJob(jobId: String): Flow<HermesResponse<Unit>>

    fun resumeJob(jobId: String): Flow<HermesResponse<Unit>>

    fun runJob(jobId: String): Flow<HermesResponse<Unit>>

    fun deleteJob(jobId: String): Flow<HermesResponse<Unit>>

    suspend fun runEvents(runId: String): Flow<SseEvent>

    suspend fun chatCompletionsStream(
        input: String,
        sessionId: String?,
        attachment: PendingAttachment?
    ): Flow<SseEvent>

    fun connectToStream(request: Request): Flow<SseEvent>
}

@Singleton
class HermesRepositoryImpl @Inject constructor(
    private val networkManager: NetworkManager,
    private val service: HermesService,
    private val endpoint: HermesEndpoint,
) : HermesRepository {

    @Volatile
    private var sseRequests = HermesRequests("http://localhost")

    override val isConfigured: StateFlow<Boolean> = endpoint.isConfigured

    override fun updateBaseUrl(host: String, port: String, apiKey: String) {
        endpoint.update(host, port, apiKey)
        sseRequests = HermesRequests("http://$host:$port", apiKey)
    }

    override fun checkHealth(): Flow<HermesResponse<HermesHealth>> =
        hermesFlow { service.health() }

    override fun getModels(): Flow<HermesResponse<HermesModels>> =
        hermesFlow { service.models() }

    override fun runs(input: String, runId: String?): Flow<HermesResponse<HermesRun>> =
        hermesFlow { service.runs(RunRequestBody(input.trim(), previousResponseId = runId)) }

    override fun runsState(runId: String): Flow<HermesResponse<HermesRunState>> =
        hermesFlow { service.runState(runId) }

    override fun stopRun(runId: String): Flow<HermesResponse<HermesRunStopModel>> =
        hermesFlow { service.stopRun(runId) }

    override fun chatResponse(input: String): Flow<HermesResponse<HermesRun>> =
        hermesFlow { service.responses(OpenAIResponsesRequestBody(input.trim())) }

    override fun jobs(): Flow<HermesResponse<HermesJobs>> =
        hermesFlow { service.jobs() }

    override fun pauseJob(jobId: String): Flow<HermesResponse<Unit>> =
        hermesFlow { service.pauseJob(jobId) }

    override fun resumeJob(jobId: String): Flow<HermesResponse<Unit>> =
        hermesFlow { service.resumeJob(jobId) }

    override fun runJob(jobId: String): Flow<HermesResponse<Unit>> =
        hermesFlow { service.runJob(jobId) }

    override fun deleteJob(jobId: String): Flow<HermesResponse<Unit>> =
        hermesFlow { service.deleteJob(jobId) }

    // ── SSE ─────────────────────────────────────────────────────────────────

    override suspend fun runEvents(runId: String): Flow<SseEvent> =
        connectToStream(sseRequests.runEvents(runId))

    override suspend fun chatCompletionsStream(
        input: String,
        sessionId: String?,
        attachment: PendingAttachment?
    ): Flow<SseEvent> =
        connectToStream(sseRequests.chatCompletions(input, sessionId, attachment, stream = true))

    override fun connectToStream(request: Request): Flow<SseEvent> =
        networkManager.connectSse(request)
}
