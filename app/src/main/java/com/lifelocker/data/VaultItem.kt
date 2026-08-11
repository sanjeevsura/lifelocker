package com.lifelocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val itemType: String, // PASSWORD, BANK_CARD, SECURE_NOTE, ID_LICENSE
    val username: String = "",
    val encryptedSecret: String = "", // Hardware-encrypted password, PIN, card number
    val category: String = "Personal",
    val notes: String = "",
    val url: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val isArchived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
