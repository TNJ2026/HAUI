package ai.tnj.haui.core.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A sealed class representing the different types of events that can occur during an SSE connection.
 */
sealed class SseEvent {
    /**
     * Emitted when the SSE connection is successfully opened.
     */
    data class Open(val response: Response) : SseEvent()

    /**
     * Emitted when a new SSE message is received.
     */
    data class Message(val id: String?, val type: String?, val data: String) : SseEvent()

    /**
     * Emitted when the SSE connection is cleanly closed.
     */
    data object Closed : SseEvent()

    /**
     * Emitted when an error occurs during the SSE connection.
     */
    data class Failure(val throwable: Throwable?, val response: Response?) : SseEvent()
}

/**
 * NetworkManager handles HTTP requests and Server-Sent Events (SSE) connections using OkHttp.
 */
@Singleton
class NetworkManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    // Shares dispatcher / connection pool / interceptors with [okHttpClient],
    // but drops the read timeout so a quiet SSE stream (e.g. model thinking)
    // isn't killed after 60s of inactivity.
    private val sseClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * Connects to a Server-Sent Events (SSE) endpoint and returns a Flow of events.
     * 
     * @param request The OkHttp Request object containing the SSE endpoint URL and necessary headers.
     * @return A Flow emitting [SseEvent]s as they are received.
     */
    fun connectSse(request: Request): Flow<SseEvent> = callbackFlow {
        val eventSourceFactory = EventSources.createFactory(sseClient)
        
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                trySend(SseEvent.Open(response))
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                trySend(SseEvent.Message(id, type, data))
            }

            override fun onClosed(eventSource: EventSource) {
                trySend(SseEvent.Closed)
                close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                trySend(SseEvent.Failure(t, response))
                // Close without throwable so consumers handle failure via the
                // emitted SseEvent.Failure rather than a thrown exception in collect.
                close()
            }
        }

        val eventSource = eventSourceFactory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}
