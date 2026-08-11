package com.lifelocker.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper

/**
 * Centralized secure clipboard handling. Never logs clipboard contents.
 */
object SecureClipboardHelper {

    private const val CLEAR_DELAY_MS = 30_000L
    private val handler = Handler(Looper.getMainLooper())
    private var clearRunnable: Runnable? = null

    fun copySecurely(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        scheduleClear(context)
    }

    fun clearClipboard(context: Context) {
        clearRunnable?.let { handler.removeCallbacks(it) }
        clearRunnable = null
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) {
            // Safe to ignore if context is invalid
        }
    }

    private fun scheduleClear(context: Context) {
        clearRunnable?.let { handler.removeCallbacks(it) }
        val appContext = context.applicationContext
        clearRunnable = Runnable { clearClipboard(appContext) }
        handler.postDelayed(clearRunnable!!, CLEAR_DELAY_MS)
    }
}
