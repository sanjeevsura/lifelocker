package com.lifelocker.utils

import androidx.fragment.app.FragmentActivity
import com.lifelocker.ui.dialogs.ItemProtectionBottomSheetFragment

/**
 * Authenticates sensitive actions via ItemProtectionBottomSheetFragment.
 */
class SensitiveActionAuthenticator(
    private val activity: FragmentActivity,
    private val securityManager: SecurityManager,
    private val biometricHelper: BiometricHelper
) {

    fun authenticate(
        itemTitle: String = "Protected Item",
        subtitle: String = "Verify your identity to view this secret.",
        itemPassword: String? = null,
        onAuthenticated: () -> Unit,
        onCancelled: () -> Unit = {},
        onRecoveryAuthorized: (() -> Unit)? = null
    ) {
        val sheet = ItemProtectionBottomSheetFragment().apply {
            this.itemTitle = itemTitle
            this.itemSubtitle = subtitle
            this.itemPassword = itemPassword
            this.onAuthenticated = onAuthenticated
            this.onCancelled = onCancelled
            this.onRecoveryAuthorized = onRecoveryAuthorized
        }
        sheet.show(activity.supportFragmentManager, ItemProtectionBottomSheetFragment.TAG)
    }
}
