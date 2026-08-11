package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int

public class EmergencyFragmentDirections private constructor() {
  private data class ActionEmergencyToAddEdit(
    public val contactId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_emergency_to_addEdit

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("contactId", this.contactId)
        return result
      }
  }

  public companion object {
    public fun actionEmergencyToAddEdit(contactId: Int = 0): NavDirections =
        ActionEmergencyToAddEdit(contactId)
  }
}
