package com.lifelocker.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.lifelocker.data.LifeLockerDatabase
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.utils.SecurityManager
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that backs up all reminder entries to an encrypted JSON file stored
 * in the app's private files directory. Runs once a day when the device is idle and
 * connected to power (to avoid battery drain). All operations are offline.
 */
class BackupReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = LifeLockerDatabase.getDatabase(context)
            val reminders = db.reminderDao().getAllRemindersSync()
            // Serialize to JSON (simple manual, no external libs)
            val json = reminders.joinToString(separator = ",", prefix = "[", postfix = "]") { reminder ->
                "{\"id\":${reminder.id},\"title\":\"${reminder.title}\",\"description\":\"${reminder.description}\",\"time\":${reminder.dueDateMillis}}"
            }
            // Encrypt using CryptoUtils (AES‑256 GCM) and write via FileStorageHelper
            val encrypted = com.lifelocker.utils.CryptoUtils.encrypt(json)
            FileStorageHelper.saveEncryptedFile(context, "reminders_backup.enc", encrypted)
            Log.d(TAG, "BackupReminderWorker completed successfully with ${reminders.size} items")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "BackupReminderWorker failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BackupReminderWorker"
        private const val WORK_NAME = "lifelocker_backup_reminder_work"

        /** Schedule a daily backup that runs only when charging and battery is not low. */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<BackupReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
