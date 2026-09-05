package com.pathgeniusacademy.lifemate.ai

import com.pathgeniusacademy.lifemate.model.HabitItem
import com.pathgeniusacademy.lifemate.model.TaskItem
import java.time.*
import java.time.format.DateTimeFormatter


data class LocalAssistantResult(
    val handled: Boolean,
    val reply: String = "",
    val taskToCreate: TaskItem? = null
)

object LocalAssistant {
    fun handle(message: String, tasks: List<TaskItem>, habits: List<HabitItem>): LocalAssistantResult {
        val raw = message.trim()
        val lower = raw.lowercase()
        if (raw.isBlank()) return LocalAssistantResult(true, "Tell me what you want to do.")

        parseEnglishReminder(raw)?.let { return it }
        parseHinglishReminder(raw)?.let { return it }

        if (lower.startsWith("add task ")) {
            val title = raw.substringAfter("add task ", "").trim()
            if (title.isNotBlank()) {
                val task = TaskItem(title = title)
                return LocalAssistantResult(true, "Added “$title” to your task list.", task)
            }
        }

        if (listOf("what do i have today", "today's plan", "todays plan", "aaj kya hai").any { lower.contains(it) }) {
            val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val today = tasks.filter { !it.completed && it.dueAt != null && it.dueAt in start until end }.sortedBy { it.dueAt }
            val undated = tasks.count { !it.completed && it.dueAt == null }
            val reply = if (today.isEmpty()) {
                "You have no timed tasks for today${if (undated > 0) ", plus $undated open task${if (undated == 1) "" else "s"}" else ""}."
            } else {
                val list = today.take(5).joinToString("\n") { "• ${it.title}${it.dueAt?.let { ms -> " — ${formatTime(ms)}" } ?: ""}" }
                "Here’s your day:\n$list${if (today.size > 5) "\n• +${today.size - 5} more" else ""}"
            }
            return LocalAssistantResult(true, reply)
        }

        if (lower.contains("plan my day") || lower.contains("mera day plan") || lower.contains("day plan karo")) {
            val open = tasks.filter { !it.completed }.sortedWith(compareBy<TaskItem> { it.dueAt ?: Long.MAX_VALUE }.thenBy { it.priority })
            if (open.isEmpty()) return LocalAssistantResult(true, "Your task list is clear. Add one important thing you want to finish today.")
            val top = open.take(4)
            val text = top.mapIndexed { i, t -> "${i + 1}. ${t.title}" }.joinToString("\n")
            return LocalAssistantResult(true, "A simple plan:\n$text\n\nStart with #1, then reassess. Keep the list realistic.")
        }

        if (lower.contains("habit") && (lower.contains("progress") || lower.contains("streak"))) {
            if (habits.isEmpty()) return LocalAssistantResult(true, "You haven’t added any habits yet. Add one small habit you can repeat consistently.")
            val text = habits.sortedByDescending { it.streak }.take(5).joinToString("\n") { "${it.emoji} ${it.name}: ${it.streak}-day streak" }
            return LocalAssistantResult(true, "Your habit snapshot:\n$text")
        }

        return LocalAssistantResult(false)
    }

    private fun parseEnglishReminder(raw: String): LocalAssistantResult? {
        val regex = Regex("""(?i)^remind me to\s+(.+?)\s+(today|tomorrow)\s+at\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?$""")
        val m = regex.find(raw) ?: return null
        val title = m.groupValues[1].trim()
        val dayWord = m.groupValues[2].lowercase()
        var hour = m.groupValues[3].toIntOrNull() ?: return null
        val minute = m.groupValues[4].toIntOrNull() ?: 0
        val ampm = m.groupValues[5].lowercase()
        if (ampm == "pm" && hour < 12) hour += 12
        if (ampm == "am" && hour == 12) hour = 0
        if (hour !in 0..23 || minute !in 0..59) return LocalAssistantResult(true, "That time doesn’t look valid. Try: “Remind me to call Sam tomorrow at 6 pm”.")
        val date = if (dayWord == "tomorrow") LocalDate.now().plusDays(1) else LocalDate.now()
        val due = ZonedDateTime.of(date, LocalTime.of(hour, minute), ZoneId.systemDefault()).toInstant().toEpochMilli()
        val task = TaskItem(title = title, dueAt = due, priority = "MEDIUM", reminderEnabled = true)
        return LocalAssistantResult(true, "Done — I’ll add a local reminder for “$title” at ${formatTime(due)} ${if (dayWord == "tomorrow") "tomorrow" else "today"}.", task)
    }

    private fun parseHinglishReminder(raw: String): LocalAssistantResult? {
        val lower = raw.lowercase()
        if (!(lower.contains("kal") && lower.contains("baje") && (lower.contains("yaad") || lower.contains("remind")))) return null
        val timeMatch = Regex("""(?i)kal\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\s*baje""").find(raw) ?: return null
        var hour = timeMatch.groupValues[1].toIntOrNull() ?: return null
        val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
        val ampm = timeMatch.groupValues[3].lowercase()
        if (ampm == "pm" && hour < 12) hour += 12
        if (ampm.isBlank() && hour in 1..7) hour += 12
        if (ampm == "am" && hour == 12) hour = 0
        val cleaned = raw
            .replace(Regex("""(?i)kal\s+\d{1,2}(?::\d{2})?\s*(am|pm)?\s*baje"""), "")
            .replace(Regex("""(?i)(mujhe|mereko|yaad dilana|yaad dila dena|remind me|remind karna)"""), "")
            .trim(' ', ',', '.', '-')
        if (cleaned.isBlank()) return LocalAssistantResult(true, "Kis cheez ka reminder chahiye? Example: “Kal 7 pm baje assignment submit karna yaad dilana”.")
        val date = LocalDate.now().plusDays(1)
        val due = ZonedDateTime.of(date, LocalTime.of(hour.coerceIn(0,23), minute.coerceIn(0,59)), ZoneId.systemDefault()).toInstant().toEpochMilli()
        val task = TaskItem(title = cleaned, dueAt = due, reminderEnabled = true)
        return LocalAssistantResult(true, "Done — “$cleaned” ka reminder kal ${formatTime(due)} ke liye add kar diya.", task)
    }

    private fun formatTime(ms: Long): String = Instant.ofEpochMilli(ms)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}
