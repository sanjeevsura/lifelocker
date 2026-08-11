package com.lifelocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact): Long

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: Int): EmergencyContact?

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts WHERE name LIKE '%' || :query || '%' OR relationship LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY isPrimary DESC, name ASC")
    fun searchContacts(query: String): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM emergency_contacts")
    suspend fun getAllContactsSyncForBackup(): List<EmergencyContact>
}
