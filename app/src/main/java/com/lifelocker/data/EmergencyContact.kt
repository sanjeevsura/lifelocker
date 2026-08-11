package com.lifelocker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val relationship: String,
    val phone: String,
    val bloodGroup: String = "Unknown",
    val allergies: String = "",
    val conditions: String = "",
    val medicines: String = "",
    val doctor: String = "",
    val hospital: String = "",
    val insurance: String = "",
    val medicalNotes: String = "",
    val isPrimary: Boolean = false
)
