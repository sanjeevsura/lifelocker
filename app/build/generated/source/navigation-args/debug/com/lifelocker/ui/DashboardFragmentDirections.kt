package com.lifelocker.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.lifelocker.R

public class DashboardFragmentDirections private constructor() {
  public companion object {
    public fun actionDashboardToVault(): NavDirections =
        ActionOnlyNavDirections(R.id.action_dashboard_to_vault)

    public fun actionDashboardToDocuments(): NavDirections =
        ActionOnlyNavDirections(R.id.action_dashboard_to_documents)

    public fun actionDashboardToReminders(): NavDirections =
        ActionOnlyNavDirections(R.id.action_dashboard_to_reminders)

    public fun actionDashboardToEmergency(): NavDirections =
        ActionOnlyNavDirections(R.id.action_dashboard_to_emergency)

    public fun actionDashboardToSettings(): NavDirections =
        ActionOnlyNavDirections(R.id.action_dashboard_to_settings)
  }
}
