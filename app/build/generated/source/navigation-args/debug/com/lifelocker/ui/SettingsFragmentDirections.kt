package com.lifelocker.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.lifelocker.R

public class SettingsFragmentDirections private constructor() {
  public companion object {
    public fun actionSettingsToNotes(): NavDirections =
        ActionOnlyNavDirections(R.id.action_settings_to_notes)
  }
}
