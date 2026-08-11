package com.lifelocker.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.os.Handler
import android.os.Looper

/**
 * Manages the active user session. Tracks the last interaction timestamp and
 * determines whether the app should auto‑lock after a configurable timeout.
 *
 * Default timeout is 5 minutes (300_000 ms). The timeout can be changed at
 * runtime via SecurityManager.setAutoLockTimeoutMinutes().
 */
object SessionManager {
    private const val DEFAULT_TIMEOUT_MS = 5 * 60 * 1000L
    private var timeoutMs = DEFAULT_TIMEOUT_MS
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    private var lastInteraction: Long = System.currentTimeMillis()
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        if (System.currentTimeMillis() - lastInteraction >= timeoutMs) {
            // Session timed out – mark inactive
            _isActive.value = false
        } else {
            // Reschedule if user interacted after this runnable was posted
            scheduleTimeout()
        }
    }

    fun startSession() {
        _isActive.value = true
        refreshInteraction()
    }

    fun endSession() {
        _isActive.value = false
        handler.removeCallbacks(timeoutRunnable)
    }

    /** Call this on any user interaction to reset the timer. */
    fun refreshInteraction() {
        lastInteraction = System.currentTimeMillis()
        scheduleTimeout()
    }

    /** Allows external update of the timeout (minutes). 0 = immediately, -1 = never. */
    fun updateTimeoutMinutes(minutes: Int) {
        timeoutMs = when (minutes) {
            -1 -> Long.MAX_VALUE / 2
            0 -> 0L
            else -> minutes * 60 * 1000L
        }
        if (_isActive.value) scheduleTimeout()
    }

    private fun scheduleTimeout() {
        handler.removeCallbacks(timeoutRunnable)
        if (timeoutMs == 0L) {
            handler.post(timeoutRunnable)
        } else if (timeoutMs > 0) {
            handler.postDelayed(timeoutRunnable, timeoutMs)
        }
    }
}
