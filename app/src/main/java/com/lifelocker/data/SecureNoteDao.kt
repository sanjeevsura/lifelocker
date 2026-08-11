package com.lifelocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SecureNoteDao {

    @Query("SELECT * FROM secure_notes WHERE isTrash = 0 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<SecureNote>>

    @Query("SELECT * FROM secure_notes WHERE isTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashNotes(): Flow<List<SecureNote>>

    @Query("SELECT * FROM secure_notes WHERE (title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND isTrash = 0 ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<SecureNote>>

    @Query("SELECT * FROM secure_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): SecureNote?

    @Query("SELECT COUNT(*) FROM secure_notes WHERE isTrash = 0")
    fun getNoteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SecureNote)

    @Update
    suspend fun updateNote(note: SecureNote)

    @Delete
    suspend fun deleteNote(note: SecureNote)

    @Query("DELETE FROM secure_notes WHERE isTrash = 1")
    suspend fun emptyTrash()
}
