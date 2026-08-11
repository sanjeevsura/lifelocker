package com.lifelocker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): ReminderItem?

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun searchReminders(query: String): Flow<List<ReminderItem>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND dueDateMillis <= :currentTimeMillis")
    suspend fun getDueRemindersSync(currentTimeMillis: Long): List<ReminderItem>

    @Query("SELECT * FROM reminders")
    suspend fun getAllRemindersSync(): List<ReminderItem>

    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 0")
    fun getActiveReminderCount(): Flow<Int>
}
