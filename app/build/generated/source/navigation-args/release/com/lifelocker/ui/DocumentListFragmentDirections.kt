package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int
import kotlin.String

public class DocumentListFragmentDirections private constructor() {
  private data class ActionDocumentsToAddEdit(
    public val documentId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_documents_to_addEdit

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("documentId", this.documentId)
        return result
      }
  }

  private data class ActionDocumentsToDetail(
    public val documentId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_documents_to_detail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("documentId", this.documentId)
        return result
      }
  }

  private data class ActionDocumentsToAddReminder(
    public val reminderId: Int = 0,
    public val title: String = "",
    public val category: String = "",
    public val dueDate: String = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_documents_to_add_reminder

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
    public fun actionDocumentsToAddEdit(documentId: Int = 0): NavDirections =
        ActionDocumentsToAddEdit(documentId)

    public fun actionDocumentsToDetail(documentId: Int = 0): NavDirections =
        ActionDocumentsToDetail(documentId)

    public fun actionDocumentsToAddReminder(
      reminderId: Int = 0,
      title: String = "",
      category: String = "",
      dueDate: String = "",
    ): NavDirections = ActionDocumentsToAddReminder(reminderId, title, category, dueDate)
  }
}
