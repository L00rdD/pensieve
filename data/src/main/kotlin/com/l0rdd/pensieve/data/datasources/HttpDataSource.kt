package com.l0rdd.pensieve.data.datasources

import kotlinx.coroutines.flow.Flow

/**
 * Plain HTTP on the house network: a GET for text or bytes, a POST of JSON, and a stream of
 * Server-Sent Events (each event's data, as the server pushes it, until the connection ends).
 * Fails on any HTTP error.
 */
interface HttpDataSource {
    suspend fun getText(url: String): String

    suspend fun getBytes(url: String): ByteArray

    suspend fun postJson(url: String, json: String)

    fun events(url: String): Flow<String>
}
