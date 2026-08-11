package com.lifelocker.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.lifelocker.R

public class AddEditDocumentFragmentDirections private constructor() {
  public companion object {
    public fun actionAddEditDocumentToCamera(): NavDirections =
        ActionOnlyNavDirections(R.id.action_add_edit_document_to_camera)
  }
}
