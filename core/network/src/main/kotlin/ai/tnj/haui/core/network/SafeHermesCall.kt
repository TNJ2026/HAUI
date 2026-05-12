package ai.tnj.haui.core.network

import ai.tnj.haui.core.model.HermesResponse
import ai.tnj.haui.core.model.HttpError
import ai.tnj.haui.core.utils.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Wraps a suspending Hermes call into a `Flow<HermesResponse<T>>`. Saves callers
 * from repeating `flow { emit(service.x()) }.asHermesResponse()` everywhere.
 */
fun <T> hermesFlow(block: suspend () -> T): Flow<HermesResponse<T>> =
    flow { emit(block()) }.asHermesResponse()

/**
 * Wraps a `Flow<T>` of plain Hermes payloads into a `Flow<HermesResponse<T>>`:
 *
 *  - emits [HermesResponse.Loading] up-front
 *  - maps each successful value to [HermesResponse.Success]
 *  - maps known exception classes to a meaningful [HttpError] code, falling
 *    back to [HttpError.ERROR_CODE_UNKNOWN] for anything else.
 *
 * Lives in `:core:network` so callers in `:core:data` don't need to depend on
 * Retrofit directly.
 */
fun <T> Flow<T>.asHermesResponse(): Flow<HermesResponse<T>> =
    map<T, HermesResponse<T>> { HermesResponse.Success(it) }
        .onStart { emit(HermesResponse.Loading) }
        .catch { e ->
            val error = when (e) {
                is HttpException -> {
                    LogUtil.e("HermesNetwork", "HTTP ${e.code()} ${e.message()}")
                    HttpError(e.code(), "HTTP error: ${e.code()}")
                }
                is SerializationException -> {
                    LogUtil.e("HermesNetwork", "JSON parse failed", e)
                    HttpError(
                        HttpError.ERROR_CODE_PARSE_JSON,
                        e.message ?: e.javaClass.simpleName,
                    )
                }
                is IOException -> {
                    LogUtil.e("HermesNetwork", "Network IO failed", e)
                    HttpError(
                        HttpError.ERROR_CODE_NETWORK,
                        e.message ?: e.javaClass.simpleName,
                    )
                }
                else -> {
                    LogUtil.e("HermesNetwork", "Request failed", e)
                    HttpError(
                        HttpError.ERROR_CODE_UNKNOWN,
                        e.message ?: e.javaClass.simpleName,
                    )
                }
            }
            emit(HermesResponse.Error(error))
        }
