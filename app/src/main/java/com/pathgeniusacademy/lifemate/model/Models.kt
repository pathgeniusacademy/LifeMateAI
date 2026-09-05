package com.pathgeniusacademy.lifemate.model

import java.util.UUID

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val dueAt: Long? = null,
    val priority: String = "MEDIUM",
    val completed: Boolean = false,
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HabitItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "✨",
    val streak: Int = 0,
    val lastCompletedDate: String? = null
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppSettings(
    val displayName: String = "",
    val themeMode: String = "SYSTEM",
    val aiBackendUrl: String = "",
    val notificationsEnabled: Boolean = true,
    val onboarded: Boolean = false
)
