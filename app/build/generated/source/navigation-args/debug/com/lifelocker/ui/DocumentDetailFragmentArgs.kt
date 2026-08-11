package com.lifelocker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.jvm.JvmStatic

public data class DocumentDetailFragmentArgs(
  public val documentId: Int = 0,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("documentId", this.documentId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("documentId", this.documentId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): DocumentDetailFragmentArgs {
      bundle.setClassLoader(DocumentDetailFragmentArgs::class.java.classLoader)
      val __documentId : Int
      if (bundle.containsKey("documentId")) {
        __documentId = bundle.getInt("documentId")
      } else {
        __documentId = 0
      }
      return DocumentDetailFragmentArgs(__documentId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        DocumentDetailFragmentArgs {
      val __documentId : Int?
      if (savedStateHandle.contains("documentId")) {
        __documentId = savedStateHandle["documentId"]
        if (__documentId == null) {
          throw IllegalArgumentException("Argument \"documentId\" of type integer does not support null values")
        }
      } else {
        __documentId = 0
      }
      return DocumentDetailFragmentArgs(__documentId)
    }
  }
}
