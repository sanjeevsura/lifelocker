package com.lifelocker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class AddEditNoteFragmentArgs(
  public val noteId: Int = 0,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("noteId", this.noteId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("noteId", this.noteId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AddEditNoteFragmentArgs {
      bundle.setClassLoader(AddEditNoteFragmentArgs::class.java.classLoader)
      val __noteId : Int
      if (bundle.containsKey("noteId")) {
        __noteId = bundle.getInt("noteId")
      } else {
        __noteId = 0
      }
      return AddEditNoteFragmentArgs(__noteId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): AddEditNoteFragmentArgs {
      val __noteId : Int?
      if (savedStateHandle.contains("noteId")) {
        __noteId = savedStateHandle["noteId"]
        if (__noteId == null) {
          throw IllegalArgumentException("Argument \"noteId\" of type integer does not support null values")
        }
      } else {
        __noteId = 0
      }
      return AddEditNoteFragmentArgs(__noteId)
    }
  }
}
