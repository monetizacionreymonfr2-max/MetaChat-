package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetaChatDao {

    @Query("SELECT * FROM contacts ORDER BY timestamp DESC")
    fun getAllContactsFlow(): Flow<List<LocalContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: LocalContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<LocalContactEntity>)

    @Delete
    suspend fun deleteContact(contact: LocalContactEntity)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<LocalMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: LocalMessageEntity)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("UPDATE messages SET isDeletedBySender = 1, content = '🚫 Este mensaje fue eliminado' WHERE id = :messageId")
    suspend fun markMessageDeleted(messageId: String)

    @Query("UPDATE contacts SET lastMessage = :lastMsg, timestamp = :time, hasTicks = :hasTicks, ticksStatus = :status WHERE id = :chatId")
    suspend fun updateContactStatus(chatId: String, lastMsg: String, time: String, hasTicks: Boolean, status: String)

    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :chatId")
    suspend fun resetUnreadCount(chatId: String)
}
