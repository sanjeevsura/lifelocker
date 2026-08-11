package com.lifelocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val action: String, // e.g., "Added", "Edited", "Viewed", "Deleted"
    val timestamp: Long = System.currentTimeMillis(),
    val entityType: String, // e.g., "Document", "VaultItem", "Reminder", "EmergencyContact"
    val entityId: Int
)
