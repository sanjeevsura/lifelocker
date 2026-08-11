package com.lifelocker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Document::class,
        VaultItem::class,
        ReminderItem::class,
        EmergencyContact::class,
        ActivityLog::class,
        SecureNote::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LifeLockerDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun vaultDao(): VaultDao
    abstract fun reminderDao(): ReminderDao
    abstract fun emergencyDao(): EmergencyDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun secureNoteDao(): SecureNoteDao

    companion object {
        @Volatile
        private var INSTANCE: LifeLockerDatabase? = null

        // Migration 1 → 2: Add all new columns to the documents table.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE documents ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE documents ADD COLUMN fileType TEXT NOT NULL DEFAULT 'DOCUMENT'")
                database.execSQL("ALTER TABLE documents ADD COLUMN mimeType TEXT NOT NULL DEFAULT '*/*'")
                database.execSQL("ALTER TABLE documents ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE documents ADD COLUMN originalExtension TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE documents ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE documents ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE documents ADD COLUMN isEncrypted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE documents ADD COLUMN isTrash INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE documents ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE documents ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration 2 → 3: Add new columns to vault_items and emergency_contacts, create activity_logs table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add to vault_items
                database.execSQL("ALTER TABLE vault_items ADD COLUMN url TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN isTrash INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE vault_items ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

                // Add to emergency_contacts
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN allergies TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN conditions TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN medicines TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN doctor TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN hospital TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE emergency_contacts ADD COLUMN insurance TEXT NOT NULL DEFAULT ''")

                // Create activity_logs table
                database.execSQL("CREATE TABLE IF NOT EXISTS `activity_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `action` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `entityType` TEXT NOT NULL, `entityId` INTEGER NOT NULL)")
            }
        }

        // Migration 3 → 4: Create secure_notes table
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS `secure_notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `encryptedContent` TEXT NOT NULL DEFAULT '', `category` TEXT NOT NULL DEFAULT 'General', `tags` TEXT NOT NULL DEFAULT '', `isFavorite` INTEGER NOT NULL DEFAULT 0, `isArchived` INTEGER NOT NULL DEFAULT 0, `isTrash` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)""")
            }
        }

        fun getDatabase(context: Context): LifeLockerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeLockerDatabase::class.java,
                    "lifelocker_offline_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
