package com.l0rdd.pensieve.data.datasources

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
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

    private fun open(url: String) = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = timeoutMs
        readTimeout = timeoutMs
    }
}
