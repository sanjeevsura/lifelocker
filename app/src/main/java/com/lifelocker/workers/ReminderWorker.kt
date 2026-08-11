package com.lifelocker.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.lifelocker.data.LifeLockerDatabase
import com.lifelocker.utils.NotificationHelper
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val database = LifeLockerDatabase.getDatabase(context)
            val currentTime = System.currentTimeMillis()

            // Log start of work
            Log.d(TAG, "ReminderWorker started at $currentTime")
            // Check due task reminders
            val dueReminders = database.reminderDao().getDueRemindersSync(currentTime)
            for (reminder in dueReminders) {
                NotificationHelper.sendNotification(
                    context = context,
                    notificationId = reminder.id,
                    channelId = NotificationHelper.CHANNEL_REMINDERS,
                    title = "Reminder: ${reminder.title}",
                    message = reminder.description.ifEmpty { "You have a task scheduled in LifeLocker." }
                )
                Log.i(TAG, "Sent reminder notification id=${reminder.id}")
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    companion object {
        private const val TAG = "ReminderWorker"
        private const val WORK_NAME = "lifelocker_periodic_reminder_work"

        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
