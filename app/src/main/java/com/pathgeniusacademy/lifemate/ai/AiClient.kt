package com.pathgeniusacademy.lifemate.ai

import com.pathgeniusacademy.lifemate.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AiClient {
    suspend fun send(backendUrl: String, message: String, history: List<ChatMessage>): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = backendUrl.trim().trimEnd('/') + "/v1/assistant"
            require(endpoint.startsWith("https://")) { "AI backend must use HTTPS" }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val payload = JSONObject().apply {
                put("message", message)
                put("history", JSONArray().apply {
                    history.takeLast(10).forEach { m ->
                        put(JSONObject().apply { put("role", m.role); put("text", m.text) })
                    }
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
