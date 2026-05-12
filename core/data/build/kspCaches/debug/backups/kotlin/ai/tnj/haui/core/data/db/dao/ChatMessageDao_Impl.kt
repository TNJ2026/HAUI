package ai.tnj.haui.core.`data`.db.dao

import ai.tnj.haui.core.`data`.db.HauiTypeConverters
import ai.tnj.haui.core.`data`.db.entity.ChatMessageEntity
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.MessageRole
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChatMessageDao_Impl(
  __db: RoomDatabase,
) : ChatMessageDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfChatMessageEntity: EntityUpsertAdapter<ChatMessageEntity>

  private val __hauiTypeConverters: HauiTypeConverters = HauiTypeConverters()
  init {
    this.__db = __db
    this.__upsertAdapterOfChatMessageEntity = EntityUpsertAdapter<ChatMessageEntity>(object : EntityInsertAdapter<ChatMessageEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `chat_messages` (`id`,`sessionId`,`role`,`type`,`text`,`imageUri`,`mimeType`,`fileName`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatMessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        val _tmp: String = __hauiTypeConverters.fromMessageRole(entity.role)
        statement.bindText(3, _tmp)
        val _tmp_1: String = __hauiTypeConverters.fromChatMessageType(entity.type)
        statement.bindText(4, _tmp_1)
        statement.bindText(5, entity.text)
        val _tmpImageUri: String? = entity.imageUri
        if (_tmpImageUri == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpImageUri)
        }
        val _tmpMimeType: String? = entity.mimeType
        if (_tmpMimeType == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpMimeType)
        }
        val _tmpFileName: String? = entity.fileName
        if (_tmpFileName == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpFileName)
        }
        statement.bindLong(9, entity.createdAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<ChatMessageEntity>() {
      protected override fun createQuery(): String = "UPDATE `chat_messages` SET `id` = ?,`sessionId` = ?,`role` = ?,`type` = ?,`text` = ?,`imageUri` = ?,`mimeType` = ?,`fileName` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ChatMessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        val _tmp: String = __hauiTypeConverters.fromMessageRole(entity.role)
        statement.bindText(3, _tmp)
        val _tmp_1: String = __hauiTypeConverters.fromChatMessageType(entity.type)
        statement.bindText(4, _tmp_1)
        statement.bindText(5, entity.text)
        val _tmpImageUri: String? = entity.imageUri
        if (_tmpImageUri == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpImageUri)
        }
        val _tmpMimeType: String? = entity.mimeType
        if (_tmpMimeType == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpMimeType)
        }
        val _tmpFileName: String? = entity.fileName
        if (_tmpFileName == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpFileName)
        }
        statement.bindLong(9, entity.createdAt)
        statement.bindText(10, entity.id)
      }
    })
  }

  public override suspend fun upsertWithTimestampCheck(entity: ChatMessageEntity): Unit = performInTransactionSuspending(__db) {
    super@ChatMessageDao_Impl.upsertWithTimestampCheck(entity)
  }

  public override suspend fun upsertAllWithTimestampCheck(entities: List<ChatMessageEntity>): Unit = performInTransactionSuspending(__db) {
    super@ChatMessageDao_Impl.upsertAllWithTimestampCheck(entities)
  }

  public override suspend fun upsert(entity: ChatMessageEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfChatMessageEntity.upsert(_connection, entity)
  }

  public override suspend fun upsertAll(entities: List<ChatMessageEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfChatMessageEntity.upsert(_connection, entities)
  }

  public override suspend fun findCreatedAt(id: String): Long? {
    val _sql: String = "SELECT createdAt FROM chat_messages WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _result: Long?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getLong(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findCreatedAtByIds(ids: List<String>): List<IdCreatedAt> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT id, createdAt FROM chat_messages WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in ids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = 0
        val _columnIndexOfCreatedAt: Int = 1
        val _result: MutableList<IdCreatedAt> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: IdCreatedAt
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item_1 = IdCreatedAt(_tmpId,_tmpCreatedAt)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBySessionFlow(sessionId: String): Flow<List<ChatMessageEntity>> {
    val _sql: String = "SELECT * FROM chat_messages WHERE sessionId = ? ORDER BY createdAt ASC"
    return createFlow(__db, false, arrayOf("chat_messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfImageUri: Int = getColumnIndexOrThrow(_stmt, "imageUri")
        val _columnIndexOfMimeType: Int = getColumnIndexOrThrow(_stmt, "mimeType")
        val _columnIndexOfFileName: Int = getColumnIndexOrThrow(_stmt, "fileName")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ChatMessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatMessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpRole: MessageRole
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfRole)
          _tmpRole = __hauiTypeConverters.toMessageRole(_tmp)
          val _tmpType: ChatMessageType
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfType)
          _tmpType = __hauiTypeConverters.toChatMessageType(_tmp_1)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpImageUri: String?
          if (_stmt.isNull(_columnIndexOfImageUri)) {
            _tmpImageUri = null
          } else {
            _tmpImageUri = _stmt.getText(_columnIndexOfImageUri)
          }
          val _tmpMimeType: String?
          if (_stmt.isNull(_columnIndexOfMimeType)) {
            _tmpMimeType = null
          } else {
            _tmpMimeType = _stmt.getText(_columnIndexOfMimeType)
          }
          val _tmpFileName: String?
          if (_stmt.isNull(_columnIndexOfFileName)) {
            _tmpFileName = null
          } else {
            _tmpFileName = _stmt.getText(_columnIndexOfFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ChatMessageEntity(_tmpId,_tmpSessionId,_tmpRole,_tmpType,_tmpText,_tmpImageUri,_tmpMimeType,_tmpFileName,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSessionsFlow(): Flow<List<SessionSummary>> {
    val _sql: String = """
        |
        |        SELECT cm.sessionId AS sessionId,
        |               MAX(cm.createdAt) AS lastAt,
        |               COUNT(*) AS messageCount,
        |               (SELECT text FROM chat_messages
        |                WHERE sessionId = cm.sessionId AND role = 'USER'
        |                ORDER BY createdAt ASC LIMIT 1) AS firstUserText,
        |               (SELECT text FROM chat_messages
        |                WHERE sessionId = cm.sessionId AND role = 'ASSISTANT'
        |                ORDER BY createdAt ASC LIMIT 1) AS firstAssistantText
        |        FROM chat_messages cm
        |        GROUP BY cm.sessionId
        |        ORDER BY lastAt DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("chat_messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSessionId: Int = 0
        val _columnIndexOfLastAt: Int = 1
        val _columnIndexOfMessageCount: Int = 2
        val _columnIndexOfFirstUserText: Int = 3
        val _columnIndexOfFirstAssistantText: Int = 4
        val _result: MutableList<SessionSummary> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionSummary
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpLastAt: Long
          _tmpLastAt = _stmt.getLong(_columnIndexOfLastAt)
          val _tmpMessageCount: Int
          _tmpMessageCount = _stmt.getLong(_columnIndexOfMessageCount).toInt()
          val _tmpFirstUserText: String?
          if (_stmt.isNull(_columnIndexOfFirstUserText)) {
            _tmpFirstUserText = null
          } else {
            _tmpFirstUserText = _stmt.getText(_columnIndexOfFirstUserText)
          }
          val _tmpFirstAssistantText: String?
          if (_stmt.isNull(_columnIndexOfFirstAssistantText)) {
            _tmpFirstAssistantText = null
          } else {
            _tmpFirstAssistantText = _stmt.getText(_columnIndexOfFirstAssistantText)
          }
          _item = SessionSummary(_tmpSessionId,_tmpLastAt,_tmpMessageCount,_tmpFirstUserText,_tmpFirstAssistantText)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBySession(sessionId: String) {
    val _sql: String = "DELETE FROM chat_messages WHERE sessionId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
