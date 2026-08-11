package com.lifelocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDateMillis: Long,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val category: String = "General",
    val isCompleted: Boolean = false,
    val repeatFrequency: String = "NONE" // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
)
