package com.lifelocker.utils

import com.lifelocker.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExpiryStatus(
    val statusText: String,
    val colorResId: Int,
    val isExpired: Boolean,
    val isCritical: Boolean,
    val isWarning: Boolean,
    val daysRemaining: Int
)

object ExpiryHelper {

    fun calculateExpiryStatus(expiryStr: String?): ExpiryStatus {
        if (expiryStr.isNullOrEmpty()) {
            return ExpiryStatus(
                statusText = "Safe — No Expiry Date",
                colorResId = R.color.text_secondary,
                isExpired = false,
                isCritical = false,
                isWarning = false,
                daysRemaining = 9999
            )
        }

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiryDate = sdf.parse(expiryStr)
            if (expiryDate == null) {
                return ExpiryStatus("Expires: $expiryStr", R.color.text_secondary, false, false, false, 9999)
            }

            val today = sdf.parse(sdf.format(Date())) ?: Date()
            val diffMs = expiryDate.time - today.time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

            when {
                diffDays < 0 -> ExpiryStatus(
                    statusText = "✕ Expired (${-diffDays} days ago)",
                    colorResId = R.color.danger,
                    isExpired = true,
                    isCritical = true,
                    isWarning = true,
                    daysRemaining = diffDays
                )
                diffDays == 0 -> ExpiryStatus(
                    statusText = "! Expires Today",
                    colorResId = R.color.danger,
                    isExpired = false,
                    isCritical = true,
                    isWarning = true,
                    daysRemaining = 0
                )
                diffDays in 1..7 -> ExpiryStatus(
                    statusText = "! Critical — $diffDays day(s) left",
                    colorResId = R.color.danger,
                    isExpired = false,
                    isCritical = true,
                    isWarning = true,
                    daysRemaining = diffDays
                )
                diffDays in 8..30 -> ExpiryStatus(
                    statusText = "⚠ Expiring Soon — $diffDays days left",
                    colorResId = R.color.warning,
                    isExpired = false,
                    isCritical = false,
                    isWarning = true,
                    daysRemaining = diffDays
                )
                diffDays in 31..90 -> ExpiryStatus(
                    statusText = "✓ Valid — $diffDays days left",
                    colorResId = R.color.accent_light,
                    isExpired = false,
                    isCritical = false,
                    isWarning = false,
                    daysRemaining = diffDays
                )
                else -> ExpiryStatus(
                    statusText = "✓ Valid — $diffDays days left",
                    colorResId = R.color.success,
                    isExpired = false,
                    isCritical = false,
                    isWarning = false,
                    daysRemaining = diffDays
                )
            }
        } catch (e: Exception) {
            ExpiryStatus("Expires: $expiryStr", R.color.text_secondary, false, false, false, 9999)
        }
    }
}
