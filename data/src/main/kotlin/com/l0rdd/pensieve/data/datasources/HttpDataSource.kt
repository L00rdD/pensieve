package com.l0rdd.pensieve.data.datasources

/** Plain HTTP on the house network: a GET for text or bytes, a POST of JSON. Fails on any HTTP error. */
interface HttpDataSource {
    suspend fun getText(url: String): String

    suspend fun getBytes(url: String): ByteArray

    suspend fun postJson(url: String, json: String)
}
