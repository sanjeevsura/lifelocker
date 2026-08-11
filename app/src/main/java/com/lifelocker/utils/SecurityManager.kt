package com.lifelocker.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityManager(context: Context) {

    private val appContext = context.applicationContext

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                appContext,
                "lifelocker_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize EncryptedSharedPreferences securely", e)
        }
    }

    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_PIN_HASH) &&
            !sharedPreferences.getString(KEY_PIN_HASH, null).isNullOrEmpty()
    }

    fun setMasterPin(pin: String): Boolean {
        if (pin.length !in 4..8 || !pin.all { it.isDigit() }) return false
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        return sharedPreferences.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .commit()
    }

    fun validatePin(pin: String): Boolean {
        if (!isPinSet() || pin.length !in 4..8 || !pin.all { it.isDigit() }) return false
        val salt = sharedPreferences.getString(KEY_PIN_SALT, "") ?: ""
        val expectedHash = sharedPreferences.getString(KEY_PIN_HASH, "") ?: ""
        val actualHash = hashPin(pin, salt)
        return expectedHash == actualHash
    }

    /** Legacy combined biometric flag — kept for backward compatibility. */
    fun isBiometricEnabled(): Boolean = isAnyBiometricEnabled()

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .putBoolean(KEY_FINGERPRINT_ENABLED, enabled)
            .putBoolean(KEY_FACE_ENABLED, enabled)
            .apply()
    }

    fun isFingerprintEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_FINGERPRINT_ENABLED, false)
    }

    fun setFingerprintEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply()
        syncLegacyBiometricFlag()
    }

    fun isFaceUnlockEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_FACE_ENABLED, false)
    }

    fun setFaceUnlockEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FACE_ENABLED, enabled).apply()
        syncLegacyBiometricFlag()
    }

    fun isAnyBiometricEnabled(): Boolean {
        return isFingerprintEnabled() || isFaceUnlockEnabled() ||
            sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun getAutoLockTimeoutMinutes(): Int {
        return sharedPreferences.getInt(KEY_AUTO_LOCK_TIMEOUT, 5)
    }

    fun setAutoLockTimeoutMinutes(minutes: Int) {
        sharedPreferences.edit().putInt(KEY_AUTO_LOCK_TIMEOUT, minutes).apply()
        SessionManager.updateTimeoutMinutes(minutes)
    }

    fun isScreenshotProtectionEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SCREENSHOT_PROTECTION, true)
    }

    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, enabled).apply()
    }

    fun applySessionTimeoutFromPrefs() {
        SessionManager.updateTimeoutMinutes(getAutoLockTimeoutMinutes())
    }

    private fun syncLegacyBiometricFlag() {
        val anyEnabled = isFingerprintEnabled() || isFaceUnlockEnabled()
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, anyEnabled).apply()
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = pin + salt
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_FINGERPRINT_ENABLED = "key_fingerprint_enabled"
        private const val KEY_FACE_ENABLED = "key_face_enabled"
        private const val KEY_AUTO_LOCK_TIMEOUT = "key_auto_lock_timeout"
        private const val KEY_SCREENSHOT_PROTECTION = "key_screenshot_protection"

        /** Auto-lock timeout options in minutes. -1 = Never */
        val AUTO_LOCK_OPTIONS = listOf(0, 1, 5, 10, 30, -1)
    }
}
