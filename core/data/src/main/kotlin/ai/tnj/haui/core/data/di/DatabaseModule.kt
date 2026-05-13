package ai.tnj.haui.core.data.di

import ai.tnj.haui.core.data.db.HauiDatabase
import ai.tnj.haui.core.data.db.dao.ChatMessageDao
import ai.tnj.haui.core.data.db.migrations.HauiMigrations
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
            // From v3 onwards every upgrade MUST ship a Migration in HauiMigrations.ALL.
            // Missing one will crash at startup, which is the desired signal during dev.
            .addMigrations(*HauiMigrations.ALL)
            // v1 / v2 predate schema export — we have no schema JSON to migrate from,
            // so devices on those versions get a destructive rebuild on their way to v3.
            // Safe to remove once telemetry shows no installs left below v3.
            .fallbackToDestructiveMigrationFrom(dropAllTables = true, 1, 2)
            // Downgrades (e.g. user sideloads an older APK) wipe local history rather
            // than crash the app. Upgrades intentionally do NOT fall back destructively.
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun provideChatMessageDao(database: HauiDatabase): ChatMessageDao =
        database.chatMessageDao()
}
