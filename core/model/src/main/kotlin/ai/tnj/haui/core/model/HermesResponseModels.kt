package ai.tnj.haui.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HermesTokenUsage(
    @SerialName("input_tokens") val inputTokens: Int,
    @SerialName("output_tokens") val outputTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

@Serializable
data class HermesRunState(
    @SerialName("object") val name: String = "",
    @SerialName("run_id") val runId: String = "",
    val status: String = "",
    @SerialName("session_id") val sessionId: String = "",
    val model: String = "",
    val output: String = "",
    val usage: HermesTokenUsage,
)

@Serializable
data class HermesRun(
    val status: String = "",
    @SerialName("run_id") val runId: String = "",
)

@Serializable
data class HermesHealth(
    val status: String,
    val platform: String = "",
    @SerialName("gateway_state") val gatewayState: String = "",
    val platforms: HermesPlatformState,
    @SerialName("active_agents") val activeAgents: Int = 0,
    @SerialName("exit_reason") val exitReason: String? = null,
    @SerialName("updated_at") val updatedAt: String = "",
    val pid: Int = -1,
)

@Serializable
data class HermesApiServerState(
    val state: String = "",
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class HermesPlatformState(
    @SerialName("api_server") val apiServer: HermesApiServerState
)

@Serializable
data class HermesModelData(
    val id: String,
    val root: String,
)

@Serializable
data class HermesModels(
    @SerialName("object") val contentType: String,
    val data: List<HermesModelData> = emptyList()
)

@Serializable
data class HermesRunStopModel(
    val status: String
)

@Serializable
data class HermesJobSchedule(
    val kind: String = "",
    val expr: String = "",
    val display: String = "",
)

@Serializable
data class HermesJobRepeat(
    val times: Int? = null,
    val completed: Int = 0,
)

@Serializable
data class HermesJob(
    val id: String = "",
    val name: String = "",
    val prompt: String = "",
    val skills: List<String> = emptyList(),
    val skill: String? = null,
    val model: String? = null,
    val provider: String? = null,
    @SerialName("base_url") val baseUrl: String? = null,
    val script: String? = null,
    @SerialName("context_from") val contextFrom: String? = null,
    val schedule: HermesJobSchedule? = null,
    @SerialName("schedule_display") val scheduleDisplay: String = "",
    val repeat: HermesJobRepeat? = null,
    val enabled: Boolean = true,
    val state: String = "",
    @SerialName("paused_at") val pausedAt: String? = null,
    @SerialName("paused_reason") val pausedReason: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("next_run_at") val nextRunAt: String? = null,
    @SerialName("last_run_at") val lastRunAt: String? = null,
    @SerialName("last_status") val lastStatus: String? = null,
    @SerialName("last_error") val lastError: String? = null,
    @SerialName("last_delivery_error") val lastDeliveryError: String? = null,
    val deliver: String = "",
    val origin: String? = null,
    @SerialName("enabled_toolsets") val enabledToolsets: List<String>? = null,
    val workdir: String? = null,
)

@Serializable
data class HermesJobs(
    val jobs: List<HermesJob> = emptyList(),
)

enum class HermesChatMsgEvent(val eventName: String) {
    ToolsStart("tool.started"),
    ToolsCompleted("tool.completed"),
    Delta("message.delta"),
    Reasoning("reasoning.available"),
    Completed("run.completed"),
}

@Serializable
data class HermesChatMessage(
    val id: String,
    val type: String,
    val role: String,
    val data: HermesMessageData? = null,
)

@Serializable
data class HermesMessageData(
    val event: String,
    @SerialName("run_id") val runId: String,
    val timestamp: Double,
    val delta: String? = null,
    val text: String? = null,
    val output: String? = null,
    val usage: HermesTokenUsage? = null,
    val tool: String? = null,
    val preview: String? = null,
    val duration: Double = 0.0,
    val error: Boolean = false
) {

    fun displayText(): String {
        return when (event) {
            HermesChatMsgEvent.ToolsStart.eventName -> {
                val base = tool ?: ""
                if (!preview.isNullOrBlank()) "$base Preview:$preview" else base
            }
            HermesChatMsgEvent.ToolsCompleted.eventName -> "  ${duration}s"
            HermesChatMsgEvent.Delta.eventName -> delta ?: ""
            HermesChatMsgEvent.Reasoning.eventName -> text ?: ""
            HermesChatMsgEvent.Completed.eventName -> output ?: ""
            else -> ""
        }
    }
}

data class HttpError(val code: Int, val msg: String) : Exception(msg) {
    companion object {
        /** JSON serialization / deserialization error. */
        const val ERROR_CODE_PARSE_JSON = 10001
        /** IO failure: timeout, connection refused, DNS, TLS, etc. */
        const val ERROR_CODE_NETWORK = 10002
        /** Anything else that isn't an HTTP status, network IO, or parse error. */
        const val ERROR_CODE_UNKNOWN = 10003
    }
}

fun Throwable.toHttpError(): HttpError =
    HttpError(HttpError.ERROR_CODE_UNKNOWN, message ?: "Unknown error")

sealed interface HermesResponse<out T> {
    data class Success<T>(val data: T) : HermesResponse<T>
    data class Error(val exception: HttpError) : HermesResponse<Nothing>
    data object Loading : HermesResponse<Nothing>
}
