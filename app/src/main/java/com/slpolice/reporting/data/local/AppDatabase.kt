package com.slpolice.reporting.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        ReportEntity::class,
        EvidenceEntity::class,
        AuditLogEntity::class,
        MessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun reportDao(): ReportDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun auditDao(): AuditDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Adds the citizen inbox introduced in version 2. Written as a real migration rather than
         * a destructive rebuild, so accounts, reports and evidence all survive the upgrade.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `messages` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`recipientId` INTEGER NOT NULL, " +
                        "`reportId` TEXT, " +
                        "`referenceNo` TEXT, " +
                        "`title` TEXT NOT NULL, " +
                        "`body` TEXT NOT NULL, " +
                        "`senderName` TEXT NOT NULL, " +
                        "`kind` TEXT NOT NULL, " +
                        "`isRead` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`recipientId`) REFERENCES `users`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_messages_recipientId` " +
                        "ON `messages` (`recipientId`)"
                )
            }
        }

        /** Records the verified capture time of each evidence file, added in version 3. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `evidence` ADD COLUMN `sourceCapturedAt` INTEGER")
            }
        }

        /** Keeps the coordinates a camera wrote into each evidence file, added in version 4. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `evidence` ADD COLUMN `capturedLatitude` REAL")
                db.execSQL("ALTER TABLE `evidence` ADD COLUMN `capturedLongitude` REAL")
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sl_police_reporting.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
