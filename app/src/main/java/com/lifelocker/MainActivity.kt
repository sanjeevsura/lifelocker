package com.lifelocker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lifelocker.databinding.ActivityMainBinding
import com.lifelocker.utils.RevealStateManager
import com.lifelocker.utils.SecureClipboardHelper
import com.lifelocker.utils.SessionManager
import com.lifelocker.workers.ReminderWorker
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object { private const val TAG = "LL_Main" }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyScreenshotProtection()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        binding.btnTopEmergency.setOnClickListener {
            if (navController.currentDestination?.id != R.id.nav_emergency) {
                navController.navigate(R.id.nav_emergency)
            }
        }

        binding.btnTopThemeToggle.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.MODE_NIGHT_NO
            } else {
                AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(newMode)
        }

        binding.etGlobalSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty() && navController.currentDestination?.id != R.id.nav_vault) {
                    navController.navigate(R.id.nav_vault)
                }
                true
            } else {
                false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_lock || destination.id == R.id.nav_splash) {
                binding.bottomNavigation.visibility = View.GONE
                binding.layoutTopBar.visibility = View.GONE
            } else {
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.layoutTopBar.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            SessionManager.isActive.collect { active ->
                if (!active) {
                    clearSensitiveUiState()
                    val currentDest = navController.currentDestination?.id
                    if (currentDest != null && currentDest != R.id.nav_lock && currentDest != R.id.nav_splash) {
                        Log.w(TAG, "Session expired, locking screen")
                        navController.navigate(R.id.nav_lock)
                    }
                }
            }
        }

        ReminderWorker.schedulePeriodicWork(this)
        com.lifelocker.workers.ExpiryWorker.schedulePeriodicWork(this)
        com.lifelocker.workers.BackupReminderWorker.schedulePeriodicWork(this)
        com.lifelocker.workers.DailySecurityWorker.schedulePeriodicWork(this)
    }

    fun applyScreenshotProtection() {
        val app = application as LifeLockerApp
        if (app.securityManager.isScreenshotProtectionEnabled()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun clearSensitiveUiState() {
        RevealStateManager.maskAll()
        SecureClipboardHelper.clearClipboard(this)
    }

    override fun onPause() {
        super.onPause()
        clearSensitiveUiState()
        val app = application as LifeLockerApp
        if (app.securityManager.getAutoLockTimeoutMinutes() == 0 && SessionManager.isActive.value) {
            SessionManager.endSession()
        }
    }

    override fun onResume() {
        super.onResume()
        applyScreenshotProtection()
        val sessionActive = SessionManager.isActive.value
        val currentDest = navController.currentDestination?.id
        if (!sessionActive) {
            if (currentDest != null && currentDest != R.id.nav_lock && currentDest != R.id.nav_splash) {
                navController.navigate(R.id.nav_lock)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        SessionManager.refreshInteraction()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
