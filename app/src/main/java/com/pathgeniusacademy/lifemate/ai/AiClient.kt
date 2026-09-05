package com.pathgeniusacademy.lifemate.ai

import com.pathgeniusacademy.lifemate.model.ChatMessage
import com.pathgeniusacademy.lifemate.model.HabitItem
import com.pathgeniusacademy.lifemate.model.NoteItem
import com.pathgeniusacademy.lifemate.model.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.ZoneId
import java.time.ZonedDateTime


data class AiAction(
    val type: String,
    val title: String = "",
    val body: String = "",
    val notes: String = "",
    val dueAt: Long? = null,
    val priority: String = "MEDIUM",
    val reminderEnabled: Boolean = false,
    val repeat: String = "NONE"
)

data class AiReply(
    val reply: String,
    val actions: List<AiAction> = emptyList()
)

object AiClient {
    suspend fun health(backendUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = backendUrl.trim().trimEnd('/') + "/health"
            require(endpoint.startsWith("https://")) { "AI backend must use HTTPS" }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/json")
            }
            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()
            if (code !in 200..299) error("AI service error ($code)")
            val json = JSONObject(body)
            if (!json.optBoolean("ok", false)) error("AI service is not ready")
            json.optString("model").ifBlank { "Connected" }
        }
    }

    suspend fun send(
        backendUrl: String,
        message: String,
        history: List<ChatMessage>,
        tasks: List<TaskItem>,
        habits: List<HabitItem>,
        notes: List<NoteItem>
    ): Result<AiReply> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = backendUrl.trim().trimEnd('/') + "/v1/assistant"
            require(endpoint.startsWith("https://")) { "AI backend must use HTTPS" }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 60_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val now = ZonedDateTime.now()
            val payload = JSONObject().apply {
                put("message", message)
                put("history", JSONArray().apply {
                    history.takeLast(12).forEach { m ->
                        put(JSONObject().apply {
                            put("role", m.role)
                            put("text", m.text)
                        })
                    }
                })
                put("context", JSONObject().apply {
                    put("nowIso", now.toString())
                    put("timezone", ZoneId.systemDefault().id)
                    put("tasks", JSONArray().apply {
                        tasks.filter { !it.completed }.take(30).forEach { t ->
                            put(JSONObject().apply {
                                put("id", t.id)
                                put("title", t.title)
                                put("notes", t.notes)
                                put("dueAt", t.dueAt ?: JSONObject.NULL)
                                put("priority", t.priority)
                                put("repeat", t.repeat)
                            })
                        }
                    })
                    put("habits", JSONArray().apply {
                        habits.take(20).forEach { h ->
                            put(JSONObject().apply {
                                put("name", h.name)
                                put("streak", h.streak)
                                put("lastCompletedDate", h.lastCompletedDate ?: JSONObject.NULL)
                            })
                        }
                    })
                    put("notes", JSONArray().apply {
                        notes.sortedByDescending { it.updatedAt }.take(8).forEach { n ->
                            put(JSONObject().apply {
                                put("title", n.title)
                                put("body", n.body.take(500))
                            })
                        }
                    })
                })
            }
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            connection.disconnect()
            if (code !in 200..299) error("AI service error ($code)")

            val json = JSONObject(body)
            val reply = json.optString("reply").ifBlank { "Done." }
            val actionsJson = json.optJSONArray("actions") ?: JSONArray()
            val actions = buildList {
                for (i in 0 until actionsJson.length()) {
                    val a = actionsJson.optJSONObject(i) ?: continue
                    val type = a.optString("type")
                    if (type !in setOf("create_task", "create_note")) continue
                    add(
                        AiAction(
                            type = type,
                            title = a.optString("title").take(200),
                            body = a.optString("body").take(4000),
                            notes = a.optString("notes").take(1200),
                            dueAt = if (a.isNull("dueAt")) null else a.optLong("dueAt").takeIf { it > 0 },
                            priority = a.optString("priority", "MEDIUM").uppercase().let { if (it in setOf("LOW", "MEDIUM", "HIGH")) it else "MEDIUM" },
                            reminderEnabled = a.optBoolean("reminderEnabled", false),
                            repeat = a.optString("repeat", "NONE").uppercase().let { if (it in setOf("NONE", "DAILY", "WEEKLY", "MONTHLY")) it else "NONE" }
                        )
                    )
                }
            }
            AiReply(reply = reply, actions = actions)
        }
    }
}
