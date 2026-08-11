package com.lifelocker.ui

import android.os.Bundle
import androidx.navigation.NavDirections
import com.lifelocker.R
import kotlin.Int

public class SecureNotesFragmentDirections private constructor() {
  private data class ActionNotesToAddEdit(
    public val noteId: Int = 0,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_notes_to_addEdit

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putInt("noteId", this.noteId)
        return result
      }
  }

  public companion object {
    public fun actionNotesToAddEdit(noteId: Int = 0): NavDirections = ActionNotesToAddEdit(noteId)
  }
}
