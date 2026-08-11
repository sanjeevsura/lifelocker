package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelocker.data.ReminderItem
import com.lifelocker.data.ReminderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawReminders: StateFlow<List<ReminderItem>> = searchQuery
        .flatMapLatest { query -> repository.searchReminders(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reminders: StateFlow<List<ReminderItem>> = combine(rawReminders, selectedCategory) { list, cat ->
        if (cat == "All") {
            list
        } else {
            list.filter { it.category.equals(cat, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeReminderCount: StateFlow<Int> = repository.activeReminderCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        selectedCategory.value = category ?: "All"
    }

    fun addReminder(reminder: ReminderItem, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            onComplete?.invoke(id)
        }
    }

    fun updateReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun toggleCompletion(reminder: ReminderItem) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.updateReminder(updated)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    suspend fun getReminderById(id: Int): ReminderItem? {
        return repository.getReminderById(id)
    }
}
