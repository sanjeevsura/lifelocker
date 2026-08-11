package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int
import kotlin.String

public class ReminderListFragmentDirections private constructor() {
  private data class ActionRemindersToAddEdit(
    public val reminderId: Int = 0,
    public val title: String = "",
    public val category: String = "",
    public val dueDate: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_reminders_to_addEdit

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("reminderId", this.reminderId)
        result.putString("title", this.title)
        result.putString("category", this.category)
        result.putString("dueDate", this.dueDate)
        return result
      }
  }

  public companion object {
    public fun actionRemindersToAddEdit(
      reminderId: Int = 0,
      title: String = "",
      category: String = "",
      dueDate: String = "",
    ): NavDirections = ActionRemindersToAddEdit(reminderId, title, category, dueDate)
  }
}
