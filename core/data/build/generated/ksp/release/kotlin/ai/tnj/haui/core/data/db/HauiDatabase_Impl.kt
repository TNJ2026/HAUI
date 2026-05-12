package ai.tnj.haui.core.`data`.db

import ai.tnj.haui.core.`data`.db.dao.ChatMessageDao
import ai.tnj.haui.core.`data`.db.dao.ChatMessageDao_Impl
import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class HauiDatabase_Impl : HauiDatabase() {
  private val _chatMessageDao: Lazy<ChatMessageDao> = lazy {
    ChatMessageDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "d9ec0eee8f59f34114f71deda6f47cc2", "530f1b7c5be88d3b396becdeeb1aead5") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `role` TEXT NOT NULL, `type` TEXT NOT NULL, `text` TEXT NOT NULL, `imageUri` TEXT, `mimeType` TEXT, `fileName` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId_createdAt` ON `chat_messages` (`sessionId`, `createdAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd9ec0eee8f59f34114f71deda6f47cc2')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `chat_messages`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsChatMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChatMessages.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("role", TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("text", TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("imageUri", TableInfo.Column("imageUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("mimeType", TableInfo.Column("mimeType", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("fileName", TableInfo.Column("fileName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChatMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChatMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesChatMessages.add(TableInfo.Index("index_chat_messages_sessionId_createdAt", false, listOf("sessionId", "createdAt"), listOf("ASC", "ASC")))
        val _infoChatMessages: TableInfo = TableInfo("chat_messages", _columnsChatMessages, _foreignKeysChatMessages, _indicesChatMessages)
        val _existingChatMessages: TableInfo = read(connection, "chat_messages")
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chat_messages(ai.tnj.haui.core.data.db.entity.ChatMessageEntity).
              | Expected:
              |""".trimMargin() + _infoChatMessages + """
              |
              | Found:
              |""".trimMargin() + _existingChatMessages)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "chat_messages")
  }

  public override fun clearAllTables() {
    super.performClear(false, "chat_messages")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ChatMessageDao::class, ChatMessageDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun chatMessageDao(): ChatMessageDao = _chatMessageDao.value
}
