package ai.tnj.haui.core.data.repository

import ai.tnj.haui.core.data.db.dao.ChatMessageDao
import ai.tnj.haui.core.data.db.dao.SessionSummary
import ai.tnj.haui.core.data.db.toEntity
import ai.tnj.haui.core.data.db.toModel
import ai.tnj.haui.core.data.di.IoDispatcher
import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.MessageRole
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface ChatHistoryRepository {

    fun getSessions(): Flow<List<SessionSummary>>

    fun loadSession(sessionId: String): Flow<List<ChatMessage>>

    suspend fun upsert(sessionId: String, message: ChatMessage)

    suspend fun upsertAll(sessionId: String, messages: List<ChatMessage>)

    suspend fun deleteSession(sessionId: String)
}

@Singleton
class ChatHistoryRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChatHistoryRepository {

    override fun getSessions(): Flow<List<SessionSummary>> =
        dao.getSessionsFlow()

    override fun loadSession(sessionId: String): Flow<List<ChatMessage>> =
        dao.getBySessionFlow(sessionId).map { entities ->
            entities.map { it.toModel() }
        }

    override suspend fun upsert(sessionId: String, message: ChatMessage) {
        // TOOL messages are explicitly excluded from persistence (UX-only).
        if (message.role == MessageRole.TOOL) return
        withContext(ioDispatcher) {
            dao.upsertWithTimestampCheck(
                message.toEntity(
                    sessionId = sessionId,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
    }

    override suspend fun upsertAll(sessionId: String, messages: List<ChatMessage>) {
        // TOOL messages are explicitly excluded from persistence (UX-only).
        val now = System.currentTimeMillis()
        val entities = messages
            .asSequence()
            .filter { it.role != MessageRole.TOOL }
            .map { it.toEntity(sessionId = sessionId, createdAt = now) }
            .toList()
        if (entities.isEmpty()) return
        withContext(ioDispatcher) {
            dao.upsertAllWithTimestampCheck(entities)
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        withContext(ioDispatcher) {
            dao.deleteBySession(sessionId)
        }
    }
}
