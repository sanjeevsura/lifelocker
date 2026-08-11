package com.lifelocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem): Long

    @Update
    suspend fun updateVaultItem(item: VaultItem)

    @Delete
    suspend fun deleteVaultItem(item: VaultItem)

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getVaultItemById(id: Int): VaultItem?

    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchVaultItems(query: String): Flow<List<VaultItem>>

    @Query("SELECT COUNT(*) FROM vault_items")
    fun getVaultCount(): Flow<Int>

    @Query("SELECT * FROM vault_items")
    suspend fun getAllVaultItemsSyncForBackup(): List<VaultItem>
}
