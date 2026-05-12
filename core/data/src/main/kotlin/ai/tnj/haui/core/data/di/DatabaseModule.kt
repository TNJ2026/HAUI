package ai.tnj.haui.core.data.di

import ai.tnj.haui.core.data.db.HauiDatabase
import ai.tnj.haui.core.data.db.dao.ChatMessageDao
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHauiDatabase(@ApplicationContext context: Context): HauiDatabase =
        Room.databaseBuilder(context, HauiDatabase::class.java, "haui.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideChatMessageDao(database: HauiDatabase): ChatMessageDao =
        database.chatMessageDao()
}