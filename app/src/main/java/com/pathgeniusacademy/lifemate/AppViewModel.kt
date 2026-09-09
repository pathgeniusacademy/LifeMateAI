package com.pathgeniusacademy.lifemate

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pathgeniusacademy.lifemate.ai.AiClient
import com.pathgeniusacademy.lifemate.ai.LocalAssistant
import com.pathgeniusacademy.lifemate.data.LocalRepository
import com.pathgeniusacademy.lifemate.model.*
import com.pathgeniusacademy.lifemate.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.pathgeniusacademy.lifemate.domain.Productivity
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = LocalRepository(application)
    private val appContext = application.applicationContext

    private val _tasks = mutableStateListOf<TaskItem>()
    val tasks: List<TaskItem> get() = _tasks

    private val _notes = mutableStateListOf<NoteItem>()
    val notes: List<NoteItem> get() = _notes

    private val _habits = mutableStateListOf<HabitItem>()
    val habits: List<HabitItem> get() = _habits

    private val _chat = mutableStateListOf<ChatMessage>()
    val chat: List<ChatMessage> get() = _chat

    var settings by mutableStateOf(repo.loadSettings())
        private set

    var assistantBusy by mutableStateOf(false)
        private set

    var aiConnectionState by mutableStateOf("UNKNOWN")
        private set

    var aiConnectionMessage by mutableStateOf("")
        private set

    private val focusPrefs = application.getSharedPreferences("lifemate_focus", 0)
    var focusMinutes by mutableIntStateOf(focusPrefs.getInt("minutes", 25))
        private set
    var focusDeadline by mutableLongStateOf(focusPrefs.getLong("deadline", 0L))
        private set
    var focusRemaining by mutableIntStateOf(focusPrefs.getInt("remaining", 25 * 60))
        private set
    var focusCompleted by mutableStateOf(false)
        private set
    var focusRevision by mutableIntStateOf(0)
        private set
    val focusRunning: Boolean get() = focusDeadline > 0

    fun setFocusDuration(minutes: Int) {
        if (focusRunning || minutes !in listOf(15, 25, 50)) return
        focusMinutes = minutes
        resetFocus()
    }
    fun toggleFocus() {
        focusCompleted = false
        if (focusRunning) {
            refreshFocus()
            focusDeadline = 0
        } else {
            if (focusRemaining <= 0) focusRemaining = focusMinutes * 60
            focusDeadline = System.currentTimeMillis() + focusRemaining * 1000L
        }
        persistFocus()
    }
    fun resetFocus() {
        focusDeadline = 0
        focusRemaining = focusMinutes * 60
        focusCompleted = false
        persistFocus()
    }
    private fun persistFocus() {
        focusPrefs.edit().putInt("minutes", focusMinutes).putInt("remaining", focusRemaining)
            .putLong("deadline", focusDeadline).apply()
    }
    private fun refreshFocus() {
        if (!focusRunning) return
        focusRemaining = Productivity.secondsLeft(focusDeadline, System.currentTimeMillis())
        if (focusRemaining == 0) {
            val date = Instant.ofEpochMilli(focusDeadline).atZone(ZoneId.systemDefault()).toLocalDate().toString()
            val key = "minutes_$date"
            focusPrefs.edit().putInt(key, focusPrefs.getInt(key, 0) + focusMinutes)
                .putLong("deadline", 0L).putInt("remaining", 0).apply()
            focusDeadline = 0
            focusCompleted = true
            focusRevision++
            persistFocus()
        }
    }
    fun focusedMinutes(date: LocalDate): Int {
        @Suppress("UNUSED_VARIABLE") val revision = focusRevision
        return focusPrefs.getInt("minutes_$date", 0)
    }
    fun habitStreak(habit: HabitItem): Int = Productivity.streak(
        habit.completionDates, LocalDate.now(), habit.legacyDate, habit.legacyStreak)

    init {
        refreshFocus()
        viewModelScope.launch { while (true) { delay(1000); refreshFocus() } }
        _tasks.addAll(repo.loadTasks())
        _notes.addAll(repo.loadNotes())
        _habits.addAll(repo.loadHabits())
        _chat.addAll(repo.loadChat())
        if (settings.aiBackendUrl.isBlank() && BuildConfig.DEFAULT_AI_BACKEND_URL.isNotBlank()) {
            settings = settings.copy(aiBackendUrl = BuildConfig.DEFAULT_AI_BACKEND_URL.trimEnd('/'))
            repo.saveSettings(settings)
        }
        if (_habits.isEmpty() && !settings.onboarded) {
            _habits.addAll(listOf(
                HabitItem(name = "Read 20 minutes", emoji = "📚"),
                HabitItem(name = "Plan tomorrow", emoji = "🗓️"),
                HabitItem(name = "Tidy one small space", emoji = "✨")
            ))
            repo.saveHabits(_habits)
        }
        if (_chat.isEmpty()) {
            _chat.add(ChatMessage(role = "assistant", text = "Hi! I’m your daily-life copilot. I can organize tasks and reminders offline, and use AI when you connect your private backend."))
            repo.saveChat(_chat)
        }
    }

    fun finishOnboarding(name: String) {
        settings = settings.copy(displayName = name.trim().ifBlank { "Friend" }, onboarded = true)
        repo.saveSettings(settings)
    }

    fun updateSettings(newSettings: AppSettings) {
        val remindersChanged = settings.notificationsEnabled != newSettings.notificationsEnabled
        settings = newSettings
        repo.saveSettings(settings)
        if (remindersChanged) _tasks.forEach { task ->
            ReminderScheduler.cancel(appContext, task.id)
            if (settings.notificationsEnabled && (task.dueAt ?: 0L) > System.currentTimeMillis()) {
                ReminderScheduler.schedule(appContext, task)
            }
        }
        aiConnectionState = "UNKNOWN"
        aiConnectionMessage = ""
    }

    fun testAiConnection(url: String = settings.aiBackendUrl) {
        val clean = url.trim().trimEnd('/')
        if (clean.isBlank()) {
            aiConnectionState = "DISCONNECTED"
            aiConnectionMessage = "Add your secure backend URL first."
            return
        }
        aiConnectionState = "TESTING"
        aiConnectionMessage = "Checking secure AI connection…"
        viewModelScope.launch {
            val result = AiClient.health(clean)
            result.onSuccess { model ->
                aiConnectionState = "CONNECTED"
                aiConnectionMessage = if (model == "Connected") "AI is connected and ready." else "AI is ready • $model"
            }.onFailure {
                aiConnectionState = "DISCONNECTED"
                aiConnectionMessage = "Couldn’t connect. Check the backend URL and deployment."
            }
        }
    }

    fun clearChat() {
        _chat.clear()
        _chat.add(ChatMessage(role = "assistant", text = "Fresh start. What can I help you organize today?"))
        repo.saveChat(_chat)
    }

    fun addTask(task: TaskItem) {
        _tasks.add(task)
        persistTasks()
        if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, task)
    }

    fun toggleTask(task: TaskItem) {
        val index = _tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return

        if (!task.completed && task.repeat != "NONE") {
            val nextDue = nextRecurringDue(task.dueAt, task.repeat)
            val updated = task.copy(completed = false, dueAt = nextDue)
            _tasks[index] = updated
            persistTasks()
            ReminderScheduler.cancel(appContext, updated.id)
            if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, updated)
            return
        }

        val updated = task.copy(completed = !task.completed)
        _tasks[index] = updated
        persistTasks()
        if (updated.completed) ReminderScheduler.cancel(appContext, updated.id)
        else if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, updated)
    }

    fun updateTask(task: TaskItem) {
        val index = _tasks.indexOfFirst { it.id == task.id }
        if (index < 0 || task.title.isBlank()) return
        _tasks[index] = task
        persistTasks()
        ReminderScheduler.cancel(appContext, task.id)
        if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, task)
    }

    fun deleteTask(task: TaskItem) {
        _tasks.removeAll { it.id == task.id }
        ReminderScheduler.cancel(appContext, task.id)
        persistTasks()
    }

    fun addNote(title: String, body: String) {
        _notes.add(0, NoteItem(title = title.ifBlank { "Untitled note" }, body = body))
        repo.saveNotes(_notes)
    }

    fun updateNote(note: NoteItem) {
        val i = _notes.indexOfFirst { it.id == note.id }
        if (i >= 0) {
            _notes[i] = note.copy(updatedAt = System.currentTimeMillis())
            repo.saveNotes(_notes)
        }
    }

    fun deleteNote(note: NoteItem) {
        _notes.removeAll { it.id == note.id }
        repo.saveNotes(_notes)
    }

    fun addHabit(name: String, emoji: String) {
        if (name.isBlank()) return
        _habits.add(HabitItem(name = name.trim(), emoji = emoji.ifBlank { "✨" }))
        repo.saveHabits(_habits)
    }

    fun toggleHabit(habit: HabitItem) {
        val i = _habits.indexOfFirst { it.id == habit.id }
        if (i < 0) return
        val today = LocalDate.now().toString()
        val dates = habit.completionDates.toMutableSet()
        if (!dates.remove(today)) dates.add(today)
        val updated = habit.copy(
            completionDates = dates.sorted(), lastCompletedDate = dates.maxOrNull(),
            streak = Productivity.streak(dates.toList(), LocalDate.now(), habit.legacyDate, habit.legacyStreak)
        )
        _habits[i] = updated
        repo.saveHabits(_habits)
    }

    fun deleteHabit(habit: HabitItem) {
        _habits.removeAll { it.id == habit.id }
        repo.saveHabits(_habits)
    }

    fun sendMessage(text: String) {
        val clean = text.trim()
        if (clean.isBlank() || assistantBusy) return
        val previousHistory = _chat.toList()
        _chat.add(ChatMessage(role = "user", text = clean))
        repo.saveChat(_chat)

        val local = LocalAssistant.handle(clean, _tasks, _habits)
        local.taskToCreate?.let { addTask(it) }
        if (local.handled) {
            _chat.add(ChatMessage(role = "assistant", text = local.reply))
            repo.saveChat(_chat)
            return
        }

        if (settings.aiBackendUrl.isBlank()) {
            _chat.add(ChatMessage(
                role = "assistant",
                text = "AI chat isn’t connected yet. The offline assistant still understands commands like “Add task buy groceries”, “What do I have today?”, and “Remind me to call Sam tomorrow at 6 pm”. Connect your AI backend in Settings for open-ended help."
            ))
            repo.saveChat(_chat)
            return
        }

        assistantBusy = true
        viewModelScope.launch {
            val result = AiClient.send(settings.aiBackendUrl, clean, previousHistory, _tasks.toList(), _habits.map { it.copy(streak = habitStreak(it)) }, _notes.toList())
            result.onSuccess { ai ->
                ai.actions.forEach { action ->
                    when (action.type) {
                        "create_task" -> if (action.title.isNotBlank()) {
                            addTask(
                                TaskItem(
                                    title = action.title,
                                    notes = action.notes,
                                    dueAt = action.dueAt,
                                    priority = action.priority,
                                    reminderEnabled = action.reminderEnabled && action.dueAt != null,
                                    repeat = action.repeat
                                )
                            )
                        }
                        "create_note" -> if (action.title.isNotBlank() || action.body.isNotBlank()) {
                            addNote(action.title.ifBlank { "AI note" }, action.body)
                        }
                    }
                }
                _chat.add(ChatMessage(role = "assistant", text = ai.reply))
            }.onFailure {
                _chat.add(ChatMessage(
                    role = "assistant",
                    text = "I couldn’t reach the AI service just now. Your offline tasks, notes, habits and reminders are still available."
                ))
            }
            repo.saveChat(_chat)
            assistantBusy = false
        }
    }

    private fun nextRecurringDue(current: Long?, repeat: String): Long? {
        if (repeat !in setOf("DAILY", "WEEKLY", "MONTHLY")) return current
        var next = current?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()) }
            ?: java.time.ZonedDateTime.now().plusHours(1)
        val now = java.time.ZonedDateTime.now()
        do {
            next = when (repeat) {
                "DAILY" -> next.plusDays(1)
                "WEEKLY" -> next.plusWeeks(1)
                "MONTHLY" -> next.plusMonths(1)
                else -> next
            }
        } while (repeat != "NONE" && !next.isAfter(now))
        return next.toInstant().toEpochMilli()
    }

    private fun persistTasks() = repo.saveTasks(_tasks)
}
