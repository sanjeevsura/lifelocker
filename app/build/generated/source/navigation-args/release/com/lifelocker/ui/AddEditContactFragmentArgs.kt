package com.lifelocker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class AddEditContactFragmentArgs(
  public val contactId: Int = 0,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("contactId", this.contactId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("contactId", this.contactId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AddEditContactFragmentArgs {
      bundle.setClassLoader(AddEditContactFragmentArgs::class.java.classLoader)
      val __contactId : Int
      if (bundle.containsKey("contactId")) {
        __contactId = bundle.getInt("contactId")
      } else {
        __contactId = 0
      }
      return AddEditContactFragmentArgs(__contactId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddEditContactFragmentArgs {
      val __contactId : Int?
      if (savedStateHandle.contains("contactId")) {
        __contactId = savedStateHandle["contactId"]
        if (__contactId == null) {
          throw IllegalArgumentException("Argument \"contactId\" of type integer does not support null values")
        }
      } else {
        __contactId = 0
      }
      return AddEditContactFragmentArgs(__contactId)
    }
  }
}
