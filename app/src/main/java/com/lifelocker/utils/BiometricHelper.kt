package com.lifelocker.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricCapability {
    AVAILABLE,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    NONE_ENROLLED,
    UNSUPPORTED
}

class BiometricHelper(private val activity: FragmentActivity) {

    fun getCapability(): BiometricCapability {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(getAllowedAuthenticators())) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NONE_ENROLLED
            else -> BiometricCapability.UNSUPPORTED
        }
    }

    fun isBiometricAvailable(): Boolean = getCapability() == BiometricCapability.AVAILABLE

    fun hasFingerprintHardware(): Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    fun hasFaceHardware(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
        } else {
            false
        }
    }

    fun getCapabilityMessage(): String {
        return when (getCapability()) {
            BiometricCapability.NO_HARDWARE ->
                "Biometric authentication isn't available on this device."
            BiometricCapability.NONE_ENROLLED ->
                "Set up fingerprint or face unlock in device settings."
            BiometricCapability.HW_UNAVAILABLE ->
                "Biometric authentication is temporarily unavailable."
            BiometricCapability.UNSUPPORTED ->
                "Biometric authentication isn't supported on this device."
            BiometricCapability.AVAILABLE -> ""
        }
    }

    fun getBiometricDescription(): String {
        val parts = mutableListOf<String>()
        if (hasFingerprintHardware()) parts.add("fingerprint")
        if (hasFaceHardware()) parts.add("face unlock")
        return when {
            parts.size >= 2 -> "Use fingerprint or face unlock."
            parts.size == 1 -> "Use ${parts[0]}."
            else -> "Use fingerprint or face unlock."
        }
    }

    fun getUnlockButtonLabel(): String {
        return when {
            hasFingerprintHardware() && hasFaceHardware() -> "Unlock with Biometrics"
            hasFaceHardware() -> "Use Face Unlock"
            hasFingerprintHardware() -> "Use Fingerprint"
            else -> "Unlock with Biometrics"
        }
    }

    fun showBiometricPrompt(
        title: String = "LifeLocker",
        subtitle: String = "Authenticate to access your secure information",
        description: String = getBiometricDescription(),
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onNegativeButton: (() -> Unit)? = null
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onNegativeButton?.invoke()
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_CANCELED -> onError("Authentication cancelled")
                    else -> onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Transient failure — BiometricPrompt stays open
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(getAllowedAuthenticators())
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        private fun getAllowedAuthenticators(): Int {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

        fun getCapability(context: Context): BiometricCapability {
            val biometricManager = BiometricManager.from(context)
            return when (biometricManager.canAuthenticate(getAllowedAuthenticators())) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.HW_UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NONE_ENROLLED
                else -> BiometricCapability.UNSUPPORTED
            }
        }
    }
}
