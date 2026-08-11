package com.lifelocker.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.lifelocker.R

public class LockFragmentDirections private constructor() {
  public companion object {
    public fun actionLockToDashboard(): NavDirections =
        ActionOnlyNavDirections(R.id.action_lock_to_dashboard)

    public fun actionLockToEmergency(): NavDirections =
        ActionOnlyNavDirections(R.id.action_lock_to_emergency)
  }
}
