package com.lifelocker.ui

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmStatic

public data class AddEditReminderFragmentArgs(
  public val reminderId: Int = 0,
  public val title: String = "",
  public val category: String = "",
  public val dueDate: String = "",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putInt("reminderId", this.reminderId)
    result.putString("title", this.title)
    result.putString("category", this.category)
    result.putString("dueDate", this.dueDate)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("reminderId", this.reminderId)
    result.set("title", this.title)
    result.set("category", this.category)
    result.set("dueDate", this.dueDate)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AddEditReminderFragmentArgs {
      bundle.setClassLoader(AddEditReminderFragmentArgs::class.java.classLoader)
      val __reminderId : Int
      if (bundle.containsKey("reminderId")) {
        __reminderId = bundle.getInt("reminderId")
      } else {
        __reminderId = 0
      }
      val __title : String?
      if (bundle.containsKey("title")) {
        __title = bundle.getString("title")
        if (__title == null) {
          throw IllegalArgumentException("Argument \"title\" is marked as non-null but was passed a null value.")
        }
      } else {
        __title = ""
      }
      val __category : String?
      if (bundle.containsKey("category")) {
        __category = bundle.getString("category")
        if (__category == null) {
          throw IllegalArgumentException("Argument \"category\" is marked as non-null but was passed a null value.")
        }
      } else {
        __category = ""
      }
      val __dueDate : String?
      if (bundle.containsKey("dueDate")) {
        __dueDate = bundle.getString("dueDate")
        if (__dueDate == null) {
          throw IllegalArgumentException("Argument \"dueDate\" is marked as non-null but was passed a null value.")
        }
      } else {
        __dueDate = ""
      }
      return AddEditReminderFragmentArgs(__reminderId, __title, __category, __dueDate)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddEditReminderFragmentArgs {
      val __reminderId : Int?
      if (savedStateHandle.contains("reminderId")) {
        __reminderId = savedStateHandle["reminderId"]
        if (__reminderId == null) {
          throw IllegalArgumentException("Argument \"reminderId\" of type integer does not support null values")
        }
      } else {
        __reminderId = 0
      }
      val __title : String?
      if (savedStateHandle.contains("title")) {
        __title = savedStateHandle["title"]
        if (__title == null) {
          throw IllegalArgumentException("Argument \"title\" is marked as non-null but was passed a null value")
        }
      } else {
        __title = ""
      }
      val __category : String?
      if (savedStateHandle.contains("category")) {
        __category = savedStateHandle["category"]
        if (__category == null) {
          throw IllegalArgumentException("Argument \"category\" is marked as non-null but was passed a null value")
        }
      } else {
        __category = ""
      }
      val __dueDate : String?
      if (savedStateHandle.contains("dueDate")) {
        __dueDate = savedStateHandle["dueDate"]
        if (__dueDate == null) {
          throw IllegalArgumentException("Argument \"dueDate\" is marked as non-null but was passed a null value")
        }
      } else {
        __dueDate = ""
      }
      return AddEditReminderFragmentArgs(__reminderId, __title, __category, __dueDate)
    }
  }
}
