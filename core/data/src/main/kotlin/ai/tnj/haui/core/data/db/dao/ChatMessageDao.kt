package ai.tnj.haui.core.data.db.dao

import ai.tnj.haui.core.data.db.entity.ChatMessageEntity
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class SessionSummary(
    val sessionId: String,
    val lastAt: Long,
    val messageCount: Int,
    val firstUserText: String?,
    val firstAssistantText: String?,
)

data class IdCreatedAt(val id: String, val createdAt: Long)

@Dao
interface ChatMessageDao {

    @Upsert
    suspend fun upsert(entity: ChatMessageEntity)

    @Upsert
    suspend fun upsertAll(entities: List<ChatMessageEntity>)

    @Transaction
    suspend fun upsertWithTimestampCheck(entity: ChatMessageEntity) {
        val existingCreatedAt = findCreatedAt(entity.id)
        if (existingCreatedAt != null) {
            upsert(entity.copy(createdAt = existingCreatedAt))
        } else {
            upsert(entity)
        }
    }

    /**
     * Batches a list of upserts in a single transaction, preserving the
     * original `createdAt` for rows that already exist. Designed for the
     * "persist the whole conversation" path so we avoid N round-trips
     * between Main and the Room IO thread.
     */
    @Transaction
    suspend fun upsertAllWithTimestampCheck(entities: List<ChatMessageEntity>) {
        if (entities.isEmpty()) return
        val ids = entities.map { it.id }
        val existing = findCreatedAtByIds(ids).associateBy({ it.id }, { it.createdAt })
        val patched = entities.map { entity ->
            existing[entity.id]?.let { entity.copy(createdAt = it) } ?: entity
        }
        upsertAll(patched)
    }

    @Query("SELECT createdAt FROM chat_messages WHERE id = :id LIMIT 1")
    suspend fun findCreatedAt(id: String): Long?

    @Query("SELECT id, createdAt FROM chat_messages WHERE id IN (:ids)")
    suspend fun findCreatedAtByIds(ids: List<String>): List<IdCreatedAt>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getBySessionFlow(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query(
        """
        SELECT cm.sessionId AS sessionId,
               MAX(cm.createdAt) AS lastAt,
               COUNT(*) AS messageCount,
               (SELECT text FROM chat_messages
                WHERE sessionId = cm.sessionId AND role = 'USER'
                ORDER BY createdAt ASC LIMIT 1) AS firstUserText,
               (SELECT text FROM chat_messages
                WHERE sessionId = cm.sessionId AND role = 'ASSISTANT'
                ORDER BY createdAt ASC LIMIT 1) AS firstAssistantText
        FROM chat_messages cm
        GROUP BY cm.sessionId
        ORDER BY lastAt DESC
        """
    )
    fun getSessionsFlow(): Flow<List<SessionSummary>>

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}
