package ai.tnj.haui.core.network

import ai.tnj.haui.core.model.ChatCompletionsRequestBody
import ai.tnj.haui.core.model.HermesHealth
import ai.tnj.haui.core.model.HermesJobs
import ai.tnj.haui.core.model.HermesModels
import ai.tnj.haui.core.model.HermesRun
import ai.tnj.haui.core.model.HermesRunState
import ai.tnj.haui.core.model.HermesRunStopModel
import ai.tnj.haui.core.model.OpenAIResponsesRequestBody
import ai.tnj.haui.core.model.RunRequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Non-streaming Hermes endpoints. SSE endpoints (`v1/runs/{id}/events`,
 * streaming chat completions) are intentionally not modelled here because
 * Retrofit doesn't natively support EventSource — they keep using
 * `NetworkManager.connectSse(...)` via `HermesRequests`.
 */
interface HermesService {

    @GET("health/detailed")
    suspend fun health(): HermesHealth

    @GET("v1/models")
    suspend fun models(): HermesModels

    @POST("v1/runs")
    suspend fun runs(@Body body: RunRequestBody): HermesRun

    @GET("v1/runs/{run_id}")
    suspend fun runState(@Path("run_id") runId: String): HermesRunState

    @POST("v1/runs/{run_id}/stop")
    suspend fun stopRun(@Path("run_id") runId: String): HermesRunStopModel

    @POST("v1/responses")
    suspend fun responses(@Body body: OpenAIResponsesRequestBody): HermesRun

    @POST("v1/chat/completions")
    suspend fun chatCompletions(@Body body: ChatCompletionsRequestBody): HermesRun

    @GET("api/jobs")
    suspend fun jobs(): HermesJobs

    @POST("api/jobs/{job_id}/pause")
    suspend fun pauseJob(@Path("job_id") jobId: String)

    @POST("api/jobs/{job_id}/resume")
    suspend fun resumeJob(@Path("job_id") jobId: String)

    @POST("api/jobs/{job_id}/run")
    suspend fun runJob(@Path("job_id") jobId: String)

    @DELETE("api/jobs/{job_id}")
    suspend fun deleteJob(@Path("job_id") jobId: String)
}
