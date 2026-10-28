package com.l0rdd.pensieve.data.datasources

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/** [HttpDataSource] with the platform's HttpURLConnection, no library. */
class UrlConnectionHttpDataSource(private val timeoutMs: Int = 4000) : HttpDataSource {

    override suspend fun getText(url: String): String = String(getBytes(url), Charsets.UTF_8)

    override suspend fun getBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        open(url).run {
            try {
                check(responseCode in 200..299) { "GET $url: HTTP $responseCode" }
                inputStream.use { it.readBytes() }
            } finally { disconnect() }
        }
    }

    override suspend fun postJson(url: String, json: String): Unit = withContext(Dispatchers.IO) {
        open(url).run {
            try {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.use { it.write(json.toByteArray()) }
                check(responseCode in 200..299) { "POST $url: HTTP $responseCode" }
            } finally { disconnect() }
        }
    }

    /** The server says something at least every 15 s (a comment when quiet): longer than [QUIET_MS] and the stream is taken as dead. */
    override fun events(url: String): Flow<String> = flow {
        open(url).run {
            try {
                readTimeout = QUIET_MS
                setRequestProperty("Accept", "text/event-stream")
                check(responseCode in 200..299) { "GET $url: HTTP $responseCode" }
                check(contentType?.startsWith("text/event-stream") == true) { "GET $url: no event stream ($contentType)" }
                inputStream.bufferedReader().use { reader -> ServerSentEvents.read(reader).forEach { emit(it) } }
            } finally { disconnect() }
        }
    }.flowOn(Dispatchers.IO)

    private fun open(url: String) = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = timeoutMs
        readTimeout = timeoutMs
    }

    private companion object {
        const val QUIET_MS = 45_000
    }
}
