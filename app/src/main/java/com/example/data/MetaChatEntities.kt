package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class LocalContactEntity(
    @PrimaryKey val id: String,
    val senderName: String,
    val avatarEmoji: String,
    val isGroup: Boolean,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val hasTicks: Boolean,
    val ticksStatus: String,
    val phoneNumber: String
)

@Entity(tableName = "messages")
data class LocalMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val sender: String,
    val content: String,
    val timestamp: String,
    val isForwarded: Boolean,
    val isDeletedBySender: Boolean,
    val status: String,
    val originalQuality: Boolean,
    val fileSize: String?
)
