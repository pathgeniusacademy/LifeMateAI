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
    ): Result<String> = withContext(Dispatchers.IO) {
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
                    put("tasks", JSONArray().apply {
                        tasks.filter { !it.completed }.take(30).forEach { t ->
                            put(JSONObject().apply {
                                put("title", t.title)
                                put("notes", t.notes)
                                put("dueAt", t.dueAt ?: JSONObject.NULL)
                                put("priority", t.priority)
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
            JSONObject(body).optString("reply").ifBlank { error("AI service returned an empty response") }
        }
    }
}
