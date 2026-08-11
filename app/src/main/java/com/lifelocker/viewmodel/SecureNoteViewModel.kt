package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelocker.data.SecureNote
import com.lifelocker.data.SecureNoteRepository
import com.lifelocker.utils.CryptoUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SecureNoteViewModel(private val repository: SecureNoteRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<SecureNote>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllNotes()
            else repository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashNotes: StateFlow<List<SecureNote>> = repository.getTrashNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCount: StateFlow<Int> = repository.getNoteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(q: String) { searchQuery.value = q }

    fun saveNote(id: Int, title: String, plainContent: String, category: String, tags: String, isFavorite: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            val encrypted = CryptoUtils.encrypt(plainContent)
            val now = System.currentTimeMillis()
            if (id == 0) {
                repository.insertNote(SecureNote(title = title, encryptedContent = encrypted, category = category, tags = tags, isFavorite = isFavorite, createdAt = now, updatedAt = now))
            } else {
                val existing = repository.getNoteById(id) ?: return@launch
                repository.updateNote(existing.copy(title = title, encryptedContent = encrypted, category = category, tags = tags, isFavorite = isFavorite, updatedAt = now))
            }
            onDone()
        }
    }

    fun decryptContent(note: SecureNote): String = CryptoUtils.decrypt(note.encryptedContent)

    fun moveToTrash(note: SecureNote) {
        viewModelScope.launch { repository.updateNote(note.copy(isTrash = true, updatedAt = System.currentTimeMillis())) }
    }

    fun restoreFromTrash(note: SecureNote) {
        viewModelScope.launch { repository.updateNote(note.copy(isTrash = false, updatedAt = System.currentTimeMillis())) }
    }

    fun deleteForever(note: SecureNote) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun toggleFavorite(note: SecureNote) {
        viewModelScope.launch { repository.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis())) }
    }

    fun emptyTrash() {
        viewModelScope.launch { repository.emptyTrash() }
    }

    suspend fun getNoteById(id: Int): SecureNote? = repository.getNoteById(id)
}
