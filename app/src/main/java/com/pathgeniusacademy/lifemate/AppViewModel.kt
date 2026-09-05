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
import java.time.LocalDate

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

    init {
        _tasks.addAll(repo.loadTasks())
        _notes.addAll(repo.loadNotes())
        _habits.addAll(repo.loadHabits())
        _chat.addAll(repo.loadChat())
        if (_habits.isEmpty()) {
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
        settings = settings.copy(displayName = name.trim(), onboarded = true)
        repo.saveSettings(settings)
    }

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        repo.saveSettings(settings)
    }

    fun addTask(task: TaskItem) {
        _tasks.add(task)
        persistTasks()
        if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, task)
    }

    fun toggleTask(task: TaskItem) {
        val index = _tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return
        val updated = task.copy(completed = !task.completed)
        _tasks[index] = updated
        persistTasks()
        if (updated.completed) ReminderScheduler.cancel(appContext, updated.id)
        else if (settings.notificationsEnabled) ReminderScheduler.schedule(appContext, updated)
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
        val yesterday = LocalDate.now().minusDays(1).toString()
        val isDoneToday = habit.lastCompletedDate == today
        val updated = if (isDoneToday) {
            habit.copy(lastCompletedDate = null, streak = (habit.streak - 1).coerceAtLeast(0))
        } else {
            val nextStreak = if (habit.lastCompletedDate == yesterday) habit.streak + 1 else 1
            habit.copy(lastCompletedDate = today, streak = nextStreak)
        }
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
            val result = AiClient.send(settings.aiBackendUrl, clean, previousHistory)
            _chat.add(ChatMessage(
                role = "assistant",
                text = result.getOrElse { "I couldn’t reach the AI service just now. Your offline tasks, notes, habits and reminders are still available." }
            ))
            repo.saveChat(_chat)
            assistantBusy = false
        }
    }

    private fun persistTasks() = repo.saveTasks(_tasks)
}
