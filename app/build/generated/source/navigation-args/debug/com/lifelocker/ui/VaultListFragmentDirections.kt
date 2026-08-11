package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int

public class VaultListFragmentDirections private constructor() {
  private data class ActionVaultToAddEdit(
    public val vaultItemId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_vault_to_addEdit

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("vaultItemId", this.vaultItemId)
        return result
      }
  }

  public companion object {
    public fun actionVaultToAddEdit(vaultItemId: Int = 0): NavDirections =
        ActionVaultToAddEdit(vaultItemId)
  }
}
