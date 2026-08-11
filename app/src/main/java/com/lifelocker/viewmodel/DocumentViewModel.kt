package com.lifelocker.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelocker.data.Document
import com.lifelocker.data.DocumentRepository
import com.lifelocker.utils.FileStorageHelper
import com.lifelocker.utils.NavigationEvent
import com.lifelocker.utils.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DocumentSortOrder {
    NEWEST,
    OLDEST,
    NAME_AZ,
    NAME_ZA,
    EXPIRY_SOONEST,
    FAVORITES_FIRST
}

class DocumentViewModel(private val repository: DocumentRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(DocumentSortOrder.FAVORITES_FIRST)
    val sortOrder: StateFlow<DocumentSortOrder> = _sortOrder.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: DocumentSortOrder) {
        _sortOrder.value = order
    }

    // Flat document list exposed to UI
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents.asStateFlow()

    // UI State for document list
    private val _documentsState = MutableStateFlow<Resource<List<Document>>>(Resource.Loading())
    val documentsState: StateFlow<Resource<List<Document>>> = _documentsState.asStateFlow()

    // Navigation events
    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    val documentCount: StateFlow<Int> = repository.documentCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val expiringDocuments: StateFlow<List<Document>> = repository.expiringDocuments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            combine(_searchQuery, _sortOrder) { query, sort ->
                Pair(query, sort)
            }.collectLatest { (query, sort) ->
                _documentsState.value = Resource.Loading()
                try {
                    repository.searchDocuments(query).collect { rawList ->
                        val sorted = when (sort) {
                            DocumentSortOrder.NEWEST -> rawList.sortedByDescending { it.createdAt }
                            DocumentSortOrder.OLDEST -> rawList.sortedBy { it.createdAt }
                            DocumentSortOrder.NAME_AZ -> rawList.sortedBy { it.title.lowercase() }
                            DocumentSortOrder.NAME_ZA -> rawList.sortedByDescending { it.title.lowercase() }
                            DocumentSortOrder.EXPIRY_SOONEST -> rawList.sortedBy { it.expiryDate.orEmpty() }
                            DocumentSortOrder.FAVORITES_FIRST -> rawList.sortedWith(
                                compareByDescending<Document> { it.isFavorite }.thenByDescending { it.createdAt }
                            )
                        }
                        _documents.value = sorted
                        _documentsState.value = Resource.Success(sorted)
                    }
                } catch (e: Exception) {
                    _documentsState.value = Resource.Error(e.localizedMessage ?: "Error loading documents")
                }
            }
        }
    }

    fun navigateToDetail(documentId: Int) {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.ToDocumentDetail(documentId))
        }
    }

    fun addDocument(document: Document, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertDocument(document)
            onComplete?.invoke(id)
        }
    }

    fun importFileFromUri(context: Context, uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val fileMeta = FileStorageHelper.saveUriToInternalStorage(context, uri)
            if (fileMeta != null) {
                val newDoc = Document(
                    title = fileMeta.fileName.substringBeforeLast('.'),
                    category = detectCategoryFromExtension(fileMeta.extension),
                    filePath = fileMeta.absolutePath,
                    expiryDate = null,
                    notes = "",
                    fileType = "DOCUMENT",
                    mimeType = fileMeta.mimeType,
                    fileSize = fileMeta.fileSize,
                    originalExtension = fileMeta.extension,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                val id = repository.insertDocument(newDoc)
                if (id > 0) {
                    onResult(true, fileMeta.fileName)
                } else {
                    onResult(false, "Database insert failed")
                }
            } else {
                onResult(false, "Failed to copy file from storage")
            }
        }
    }

    private fun detectCategoryFromExtension(ext: String): String {
        return when (ext.lowercase()) {
            "pdf", "doc", "docx", "txt", "rtf" -> "Documents"
            "jpg", "jpeg", "png", "webp", "heic" -> "Photos & Scans"
            "xls", "xlsx", "csv" -> "Invoices & Finance"
            "zip", "rar", "7z" -> "Archives"
            else -> "General"
        }
    }

    fun updateDocument(document: Document) {
        viewModelScope.launch {
            repository.updateDocument(document)
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    fun deleteMultipleDocuments(documents: List<Document>) {
        viewModelScope.launch {
            documents.forEach { doc ->
                if (doc.filePath.isNotEmpty()) {
                    FileStorageHelper.deleteInternalFile(doc.filePath)
                }
                repository.deleteDocument(doc)
            }
        }
    }

    fun toggleFavorite(document: Document) {
        viewModelScope.launch {
            repository.toggleFavorite(document.id, !document.isFavorite)
        }
    }

    fun moveToTrash(document: Document) {
        viewModelScope.launch {
            repository.moveToTrash(document.id)
        }
    }

    suspend fun getDocumentById(id: Int): Document? {
        return repository.getDocumentById(id)
    }
}


