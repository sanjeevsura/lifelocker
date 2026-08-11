package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import com.lifelocker.utils.SecurityManager
import com.lifelocker.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(private val securityManager: SecurityManager) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    fun isPinSet(): Boolean = securityManager.isPinSet()

    fun setupPin(pin: String): Boolean {
        val success = securityManager.setMasterPin(pin)
        if (success) {
            SessionManager.startSession()
            _isUnlocked.value = true
        }
        return success
    }

    fun validatePin(pin: String): Boolean {
        val isValid = securityManager.validatePin(pin)
        if (isValid) {
            SessionManager.startSession()
            _isUnlocked.value = true
        }
        return isValid
    }

    fun onBiometricSuccess() {
        SessionManager.startSession()
        _isUnlocked.value = true
    }

    fun lockApp() {
        SessionManager.endSession()
        _isUnlocked.value = false
    }

    fun isBiometricEnabled(): Boolean = securityManager.isAnyBiometricEnabled()

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
    }

    fun isFingerprintEnabled(): Boolean = securityManager.isFingerprintEnabled()

    fun setFingerprintEnabled(enabled: Boolean) {
        securityManager.setFingerprintEnabled(enabled)
    }

    fun isFaceUnlockEnabled(): Boolean = securityManager.isFaceUnlockEnabled()

    fun setFaceUnlockEnabled(enabled: Boolean) {
        securityManager.setFaceUnlockEnabled(enabled)
    }

    fun getAutoLockTimeoutMinutes(): Int = securityManager.getAutoLockTimeoutMinutes()

    fun setAutoLockTimeoutMinutes(minutes: Int) {
        securityManager.setAutoLockTimeoutMinutes(minutes)
    }

    fun isScreenshotProtectionEnabled(): Boolean = securityManager.isScreenshotProtectionEnabled()

    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        securityManager.setScreenshotProtectionEnabled(enabled)
    }

    fun validatePinForSensitiveAction(pin: String): Boolean = securityManager.validatePin(pin)
}
