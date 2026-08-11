package com.lifelocker.utils

import android.os.Handler
import android.os.Looper

/**
 * Tracks temporarily revealed credential IDs. Never persists reveal state.
 */
object RevealStateManager {

    const val REVEAL_TIMEOUT_MS = 10_000L
    private const val MASKED_DISPLAY = "••••••••••"

    private val revealedIds = mutableSetOf<Int>()
    private val maskRunnables = mutableMapOf<Int, Runnable>()
    private val handler = Handler(Looper.getMainLooper())
    private val listeners = mutableSetOf<() -> Unit>()

    fun getMaskedDisplay(): String = MASKED_DISPLAY

    fun isRevealed(id: Int): Boolean = revealedIds.contains(id)

    fun reveal(id: Int, onAutoMask: (() -> Unit)? = null) {
        revealedIds.add(id)
        maskRunnables.remove(id)?.let { handler.removeCallbacks(it) }
        val runnable = Runnable {
            mask(id)
            onAutoMask?.invoke()
            notifyListeners()
        }
        maskRunnables[id] = runnable
        handler.postDelayed(runnable, REVEAL_TIMEOUT_MS)
        notifyListeners()
    }

    fun mask(id: Int) {
        revealedIds.remove(id)
        maskRunnables.remove(id)?.let { handler.removeCallbacks(it) }
        notifyListeners()
    }

    fun maskAll() {
        revealedIds.clear()
        maskRunnables.values.forEach { handler.removeCallbacks(it) }
        maskRunnables.clear()
        notifyListeners()
    }

    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }
}
