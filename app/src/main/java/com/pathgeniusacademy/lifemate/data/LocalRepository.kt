package com.pathgeniusacademy.lifemate.data

import android.content.Context
import com.pathgeniusacademy.lifemate.model.*
import org.json.JSONArray
import org.json.JSONObject

class LocalRepository(context: Context) {
    private val prefs = context.getSharedPreferences("lifemate_local_data", Context.MODE_PRIVATE)

    fun loadTasks(): List<TaskItem> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_TASKS, "[]"))
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(TaskItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    notes = o.optString("notes", ""),
                    dueAt = if (o.isNull("dueAt")) null else o.getLong("dueAt"),
                    priority = o.optString("priority", "MEDIUM"),
                    completed = o.optBoolean("completed", false),
                    reminderEnabled = o.optBoolean("reminderEnabled", false),
                    repeat = o.optString("repeat", "NONE"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun saveTasks(items: List<TaskItem>) {
        val arr = JSONArray()
        items.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id); put("title", t.title); put("notes", t.notes)
                put("dueAt", t.dueAt ?: JSONObject.NULL); put("priority", t.priority)
                put("completed", t.completed); put("reminderEnabled", t.reminderEnabled)
                put("repeat", t.repeat); put("createdAt", t.createdAt)
            })
        }
        prefs.edit().putString(KEY_TASKS, arr.toString()).apply()
    }

    fun loadNotes(): List<NoteItem> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_NOTES, "[]"))
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(NoteItem(
                    id = o.getString("id"), title = o.getString("title"),
                    body = o.optString("body", ""), pinned = o.optBoolean("pinned", false),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun saveNotes(items: List<NoteItem>) {
        val arr = JSONArray()
        items.forEach { n -> arr.put(JSONObject().apply {
            put("id", n.id); put("title", n.title); put("body", n.body); put("pinned", n.pinned)
            put("createdAt", n.createdAt); put("updatedAt", n.updatedAt)
        }) }
        prefs.edit().putString(KEY_NOTES, arr.toString()).apply()
    }

    fun loadHabits(): List<HabitItem> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_HABITS, "[]"))
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(HabitItem(
                    id = o.getString("id"), name = o.getString("name"), emoji = o.optString("emoji", "✨"),
                    streak = o.optInt("streak", 0),
                    lastCompletedDate = if (o.isNull("lastCompletedDate")) null else o.optString("lastCompletedDate")
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun saveHabits(items: List<HabitItem>) {
        val arr = JSONArray()
        items.forEach { h -> arr.put(JSONObject().apply {
            put("id", h.id); put("name", h.name); put("emoji", h.emoji); put("streak", h.streak)
            put("lastCompletedDate", h.lastCompletedDate ?: JSONObject.NULL)
        }) }
        prefs.edit().putString(KEY_HABITS, arr.toString()).apply()
    }

    fun loadChat(): List<ChatMessage> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_CHAT, "[]"))
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(ChatMessage(
                    id = o.getString("id"), role = o.getString("role"), text = o.getString("text"),
                    timestamp = o.optLong("timestamp", System.currentTimeMillis())
                ))
            }
        }
    }.getOrDefault(emptyList())

    fun saveChat(items: List<ChatMessage>) {
        val arr = JSONArray()
        items.takeLast(60).forEach { m -> arr.put(JSONObject().apply {
            put("id", m.id); put("role", m.role); put("text", m.text); put("timestamp", m.timestamp)
        }) }
        prefs.edit().putString(KEY_CHAT, arr.toString()).apply()
    }

    fun loadSettings(): AppSettings = runCatching {
        val o = JSONObject(prefs.getString(KEY_SETTINGS, "{}"))
        AppSettings(
            displayName = o.optString("displayName", ""),
            themeMode = o.optString("themeMode", "SYSTEM"),
            aiBackendUrl = o.optString("aiBackendUrl", ""),
            notificationsEnabled = o.optBoolean("notificationsEnabled", true),
            onboarded = o.optBoolean("onboarded", false)
        )
    }.getOrDefault(AppSettings())

    fun saveSettings(s: AppSettings) {
        val o = JSONObject().apply {
            put("displayName", s.displayName); put("themeMode", s.themeMode); put("aiBackendUrl", s.aiBackendUrl)
            put("notificationsEnabled", s.notificationsEnabled); put("onboarded", s.onboarded)
        }
        prefs.edit().putString(KEY_SETTINGS, o.toString()).apply()
    }

    companion object {
        private const val KEY_TASKS = "tasks"
        private const val KEY_NOTES = "notes"
        private const val KEY_HABITS = "habits"
        private const val KEY_CHAT = "chat"
        private const val KEY_SETTINGS = "settings"
    }
}
