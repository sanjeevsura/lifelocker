package com.lifelocker

import android.app.Application
import com.lifelocker.data.LifeLockerDatabase
import com.lifelocker.utils.NotificationHelper
import com.lifelocker.utils.SecurityManager

class LifeLockerApp : Application() {

    val database: LifeLockerDatabase by lazy { LifeLockerDatabase.getDatabase(this) }
    val securityManager: SecurityManager by lazy { SecurityManager(this) }

    override fun onCreate() {
        super.onCreate()
        securityManager.applySessionTimeoutFromPrefs()
        NotificationHelper.createNotificationChannels(this)
    }
}
