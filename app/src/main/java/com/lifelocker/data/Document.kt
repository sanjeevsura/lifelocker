package com.lifelocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val filePath: String,
    val expiryDate: String?,
    val notes: String = "",
    val fileType: String = "DOCUMENT",
    val mimeType: String = "*/*",
    val fileSize: Long = 0L,
    val originalExtension: String = "",
    val isFavorite: Boolean = false,
    val tags: String = "",
    val isEncrypted: Boolean = false,
    val isTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

