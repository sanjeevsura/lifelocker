package com.lifelocker.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {

    val allDocuments: Flow<List<Document>> = documentDao.getAllDocuments()
    val documentCount: Flow<Int> = documentDao.getDocumentCount()
    val expiringDocuments: Flow<List<Document>> = documentDao.getExpiringDocumentsFlow()

    fun searchDocuments(query: String): Flow<List<Document>> {
        return if (query.isBlank()) {
            documentDao.getAllDocuments()
        } else {
            documentDao.searchDocuments(query)
        }
    }

    suspend fun getDocumentById(id: Int): Document? = documentDao.getDocumentById(id)

    suspend fun insertDocument(document: Document): Long = documentDao.insertDocument(document)

    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)

    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)

    suspend fun toggleFavorite(documentId: Int, isFavorite: Boolean) {
        documentDao.updateFavoriteStatus(documentId, isFavorite)
    }

    suspend fun moveToTrash(documentId: Int) {
        documentDao.moveToTrash(documentId)
    }
}

