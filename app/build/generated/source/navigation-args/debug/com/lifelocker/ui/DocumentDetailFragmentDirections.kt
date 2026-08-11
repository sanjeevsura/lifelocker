package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int
import kotlin.String

public class DocumentDetailFragmentDirections private constructor() {
  private data class ActionDetailToAddReminder(
    public val reminderId: Int = 0,
    public val title: String = "",
    public val category: String = "",
    public val dueDate: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_detail_to_add_reminder

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

  private data class ActionDetailToEditDocument(
    public val documentId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_detail_to_edit_document

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("documentId", this.documentId)
        return result
      }
  }

  public companion object {
    public fun actionDetailToAddReminder(
      reminderId: Int = 0,
      title: String = "",
      category: String = "",
      dueDate: String = "",
    ): NavDirections = ActionDetailToAddReminder(reminderId, title, category, dueDate)

    public fun actionDetailToEditDocument(documentId: Int = 0): NavDirections =
        ActionDetailToEditDocument(documentId)
  }
}
