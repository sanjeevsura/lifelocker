package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelocker.data.EmergencyContact
import com.lifelocker.data.EmergencyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmergencyViewModel(private val repository: EmergencyRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: StateFlow<List<EmergencyContact>> = searchQuery
        .flatMapLatest { query -> repository.searchContacts(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addContact(contact: EmergencyContact, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertContact(contact)
            onComplete?.invoke(id)
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    suspend fun getContactById(id: Int): EmergencyContact? {
        return repository.getContactById(id)
    }
}
