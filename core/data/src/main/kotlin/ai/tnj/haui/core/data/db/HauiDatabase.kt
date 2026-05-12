package ai.tnj.haui.core.data.db

import ai.tnj.haui.core.data.db.dao.ChatMessageDao
import ai.tnj.haui.core.data.db.entity.ChatMessageEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ChatMessageEntity::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(HauiTypeConverters::class)
abstract class HauiDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
}
