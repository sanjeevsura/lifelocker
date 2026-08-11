package com.lifelocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): Document?

    @Query("SELECT * FROM documents WHERE isTrash = 0 ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND (title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR originalExtension LIKE '%' || :query || '%') ORDER BY isFavorite DESC, createdAt DESC")
    fun searchDocuments(query: String): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND expiryDate IS NOT NULL AND expiryDate != ''")
    suspend fun getExpiringDocumentsSync(): List<Document>

    @Query("SELECT * FROM documents WHERE isTrash = 0 AND expiryDate IS NOT NULL AND expiryDate != ''")
    fun getExpiringDocumentsFlow(): Flow<List<Document>>

    @Query("SELECT COUNT(*) FROM documents WHERE isTrash = 0")
    fun getDocumentCount(): Flow<Int>

    @Query("UPDATE documents SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFav: Boolean)

    @Query("UPDATE documents SET isTrash = 1 WHERE id = :id")
    suspend fun moveToTrash(id: Int)

    @Query("SELECT * FROM documents")
    suspend fun getAllDocumentsSyncForBackup(): List<Document>

    @Query("SELECT * FROM documents WHERE isTrash = 1 ORDER BY updatedAt DESC")
    fun getTrashDocuments(): Flow<List<Document>>
}

