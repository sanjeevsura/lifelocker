package com.lifelocker.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.lifelocker.utils.SecureClipboardHelper
import com.lifelocker.utils.SecurityManager
import java.util.concurrent.TimeUnit

/**
 * DailySecurityWorker runs once a day while the device is charging and battery is not low.
 * It performs offline security‑related maintenance such as:
 *  - Ensuring the Android Keystore master key exists (triggered by SecurityManager init).
 *  - Clearing any sensitive clipboard content.
 *  - Logging security health metrics.
 *
 * All actions are performed locally; no network is required.
 */
class DailySecurityWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "DailySecurityWorker started")
            // Initialise SecurityManager – this will create the master key if missing.
            SecurityManager(context)
            SecureClipboardHelper.clearClipboard(context)
            Log.d(TAG, "Daily security tasks completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "DailySecurityWorker failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailySecurityWorker"
        private const val WORK_NAME = "lifelocker_daily_security_work"

        /** Schedule a 24‑hour periodic work with constraints suitable for security tasks. */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<DailySecurityWorker>(24, TimeUnit.HOURS)
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
