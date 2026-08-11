package com.lifelocker.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.lifelocker.data.LifeLockerDatabase
import com.lifelocker.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that scans for documents whose expiry date is approaching or passed and
 * dispatches a notification on the CHANNEL_EXPIRIES channel.
 *
 * Runs offline – no network constraints – but respects battery constraints.
 */
class ExpiryWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = LifeLockerDatabase.getDatabase(context)
            val expiring = db.documentDao().getExpiringDocumentsSync()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val nowMs = System.currentTimeMillis()

            for (doc in expiring) {
                val expStr = doc.expiryDate ?: continue
                try {
                    val expDate = sdf.parse(expStr) ?: continue
                    val diffDays = ((expDate.time - nowMs) / (1000 * 60 * 60 * 24)).toInt()
                    if (diffDays <= 30) {
                        val message = when {
                            diffDays < 0 -> "Expired ${-diffDays} days ago!"
                            diffDays == 0 -> "Expires TODAY!"
                            else -> "Expires in $diffDays days."
                        }
                        NotificationHelper.sendNotification(
                            context = context,
                            notificationId = doc.id + 1000,
                            channelId = NotificationHelper.CHANNEL_EXPIRIES,
                            title = "Document Expiry: ${doc.title}",
                            message = message
                        )
                        Log.d(TAG, "Sent expiry notification for document id=${doc.id}: $message")
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "ExpiryWorker failed", e)
            Result.retry()
        }
    }


    companion object {
        private const val TAG = "ExpiryWorker"
        private const val WORK_NAME = "lifelocker_expiry_worker"

        /** Schedule a periodic work that runs once a day. */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .build()

            val request = PeriodicWorkRequestBuilder<ExpiryWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
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
