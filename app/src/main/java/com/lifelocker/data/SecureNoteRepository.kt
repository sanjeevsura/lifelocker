package com.lifelocker.data

import kotlinx.coroutines.flow.Flow

class SecureNoteRepository(private val dao: SecureNoteDao) {
    fun getAllNotes(): Flow<List<SecureNote>> = dao.getAllNotes()
    fun getTrashNotes(): Flow<List<SecureNote>> = dao.getTrashNotes()
    fun searchNotes(query: String): Flow<List<SecureNote>> = dao.searchNotes(query)
    fun getNoteCount(): Flow<Int> = dao.getNoteCount()
    suspend fun getNoteById(id: Int): SecureNote? = dao.getNoteById(id)
    suspend fun insertNote(note: SecureNote) = dao.insertNote(note)
    suspend fun updateNote(note: SecureNote) = dao.updateNote(note)
    suspend fun deleteNote(note: SecureNote) = dao.deleteNote(note)
    suspend fun emptyTrash() = dao.emptyTrash()
}
