package com.l0rdd.pensieve.data.fakes

import com.l0rdd.pensieve.data.datasources.HttpDataSource

/** Answers from a map of URL to body; an unknown URL fails, as a dead host would. Records posts. */
class FakeHttpDataSource(var answers: Map<String, String> = emptyMap()) : HttpDataSource {
    val posted = mutableListOf<Pair<String, String>>()
    val fetched = mutableListOf<String>()
    override suspend fun getText(url: String): String { fetched += url; return answers[url] ?: error("no answer from $url") }
    override suspend fun getBytes(url: String): ByteArray = getText(url).toByteArray()
    override suspend fun postJson(url: String, json: String) { posted += url to json }
}
