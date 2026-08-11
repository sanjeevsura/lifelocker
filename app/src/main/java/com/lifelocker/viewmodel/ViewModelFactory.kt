package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lifelocker.data.DocumentRepository
import com.lifelocker.data.EmergencyRepository
import com.lifelocker.data.ReminderRepository
import com.lifelocker.data.SecureNoteRepository
import com.lifelocker.data.VaultRepository
import com.lifelocker.utils.SecurityManager

class ViewModelFactory(
    private val documentRepository: DocumentRepository? = null,
    private val vaultRepository: VaultRepository? = null,
    private val reminderRepository: ReminderRepository? = null,
    private val emergencyRepository: EmergencyRepository? = null,
    private val secureNoteRepository: SecureNoteRepository? = null,
    private val securityManager: SecurityManager? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DocumentViewModel::class.java) -> {
                DocumentViewModel(documentRepository!!) as T
            }
            modelClass.isAssignableFrom(VaultViewModel::class.java) -> {
                VaultViewModel(vaultRepository!!) as T
            }
            modelClass.isAssignableFrom(ReminderViewModel::class.java) -> {
                ReminderViewModel(reminderRepository!!) as T
            }
            modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> {
                EmergencyViewModel(emergencyRepository!!) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(securityManager!!) as T
            }
            modelClass.isAssignableFrom(SecureNoteViewModel::class.java) -> {
                SecureNoteViewModel(secureNoteRepository!!) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
