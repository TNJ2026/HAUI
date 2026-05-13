package ai.tnj.haui.core.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Single source of truth for Room migrations.
 *
 * Database history:
 *   v1 / v2 — pre-baseline, never had schema JSON exported. Devices that
 *             still carry these versions are migrated destructively (see
 *             [DatabaseModule]) because we cannot author migrations without
 *             knowing the source schema.
 *   v3       — baseline. Single table `chat_messages` with composite index
 *             on (sessionId, createdAt). First version with `schemas/` exported.
 *
 * When bumping the version:
 *   1. Update [HauiDatabase.version] and the entity definitions.
 *   2. Rebuild once → schemas/.../<new>.json appears, commit it.
 *   3. Author the corresponding `MIGRATION_N_M` below and add it to [ALL].
 *   4. Add an instrumentation test in
 *      `core:data/src/androidTest/.../MigrationTest.kt` that verifies the
 *      migration against the previous schema JSON.
 */
object HauiMigrations {

    /**
     * Ordered list passed to `RoomDatabase.Builder.addMigrations(...)`.
     * Keep adjacent (`N → N+1`) migrations; Room will compose them as needed.
     */
    val ALL: Array<Migration> = arrayOf(
        // No v3+ migrations yet. Append `MIGRATION_3_4` etc. as the schema evolves.
    )

    // ─────────────────────────────────────────────────────────────────────
    // Template — copy when adding the next migration:
    //
    //   val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    //       override fun migrate(db: SupportSQLiteDatabase) {
    //           db.execSQL("ALTER TABLE chat_messages ADD COLUMN reply_to TEXT")
    //       }
    //   }
    //
    // Then:
    //   val ALL = arrayOf(MIGRATION_3_4)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Helper for destructive table recreation when ALTER is awkward
     * (e.g. changing column types or constraints). Wrap in a Migration and
     * remember to copy data over BEFORE dropping the old table.
     */
    @Suppress("unused")
    internal fun SupportSQLiteDatabase.recreateChatMessages() {
        execSQL("DROP TABLE IF EXISTS chat_messages")
        execSQL(
            """
            CREATE TABLE chat_messages (
                id TEXT NOT NULL PRIMARY KEY,
                sessionId TEXT NOT NULL,
                role TEXT NOT NULL,
                type TEXT NOT NULL,
                text TEXT NOT NULL,
                imageUri TEXT,
                mimeType TEXT,
                fileName TEXT,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        execSQL(
            "CREATE INDEX IF NOT EXISTS index_chat_messages_sessionId_createdAt " +
                "ON chat_messages (sessionId, createdAt)"
        )
    }
}
