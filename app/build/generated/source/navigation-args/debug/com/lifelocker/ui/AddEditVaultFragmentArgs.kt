package com.lifelocker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class AddEditVaultFragmentArgs(
  public val vaultItemId: Int = 0,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("vaultItemId", this.vaultItemId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("vaultItemId", this.vaultItemId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AddEditVaultFragmentArgs {
      bundle.setClassLoader(AddEditVaultFragmentArgs::class.java.classLoader)
      val __vaultItemId : Int
      if (bundle.containsKey("vaultItemId")) {
        __vaultItemId = bundle.getInt("vaultItemId")
      } else {
        __vaultItemId = 0
      }
      return AddEditVaultFragmentArgs(__vaultItemId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): AddEditVaultFragmentArgs {
      val __vaultItemId : Int?
      if (savedStateHandle.contains("vaultItemId")) {
        __vaultItemId = savedStateHandle["vaultItemId"]
        if (__vaultItemId == null) {
          throw IllegalArgumentException("Argument \"vaultItemId\" of type integer does not support null values")
        }
      } else {
        __vaultItemId = 0
      }
      return AddEditVaultFragmentArgs(__vaultItemId)
    }
  }
}
