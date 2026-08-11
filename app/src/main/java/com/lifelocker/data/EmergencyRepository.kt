package com.lifelocker.data

import kotlinx.coroutines.flow.Flow

class EmergencyRepository(private val emergencyDao: EmergencyDao) {

    val allContacts: Flow<List<EmergencyContact>> = emergencyDao.getAllContacts()

    fun searchContacts(query: String): Flow<List<EmergencyContact>> {
        return if (query.isBlank()) {
            emergencyDao.getAllContacts()
        } else {
            emergencyDao.searchContacts(query)
        }
    }

    suspend fun getContactById(id: Int): EmergencyContact? = emergencyDao.getContactById(id)

    suspend fun insertContact(contact: EmergencyContact): Long = emergencyDao.insertContact(contact)

    suspend fun updateContact(contact: EmergencyContact) = emergencyDao.updateContact(contact)

    suspend fun deleteContact(contact: EmergencyContact) = emergencyDao.deleteContact(contact)
}
