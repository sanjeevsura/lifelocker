package com.lifelocker.data

import kotlinx.coroutines.flow.Flow

class VaultRepository(private val vaultDao: VaultDao) {

    val allVaultItems: Flow<List<VaultItem>> = vaultDao.getAllVaultItems()
    val vaultCount: Flow<Int> = vaultDao.getVaultCount()

    fun searchVaultItems(query: String): Flow<List<VaultItem>> {
        return if (query.isBlank()) {
            vaultDao.getAllVaultItems()
        } else {
            vaultDao.searchVaultItems(query)
        }
    }

    suspend fun getVaultItemById(id: Int): VaultItem? = vaultDao.getVaultItemById(id)

    suspend fun insertVaultItem(item: VaultItem): Long = vaultDao.insertVaultItem(item)

    suspend fun updateVaultItem(item: VaultItem) = vaultDao.updateVaultItem(item)

    suspend fun deleteVaultItem(item: VaultItem) = vaultDao.deleteVaultItem(item)
}
