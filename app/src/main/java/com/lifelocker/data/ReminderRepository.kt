package com.lifelocker.data

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<ReminderItem>> = reminderDao.getAllReminders()
    val activeReminderCount: Flow<Int> = reminderDao.getActiveReminderCount()

    fun searchReminders(query: String): Flow<List<ReminderItem>> {
        return if (query.isBlank()) {
            reminderDao.getAllReminders()
        } else {
            reminderDao.searchReminders(query)
        }
    }

    suspend fun getReminderById(id: Int): ReminderItem? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: ReminderItem): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderItem) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: ReminderItem) = reminderDao.deleteReminder(reminder)

    suspend fun getDueRemindersSync(currentTimeMillis: Long): List<ReminderItem> =
        reminderDao.getDueRemindersSync(currentTimeMillis)
}
