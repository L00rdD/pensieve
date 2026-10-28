package com.l0rdd.pensieve.data.fakes

import com.l0rdd.pensieve.data.datasources.HttpDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/** Answers from a map of URL to body; an unknown URL fails, as a dead host would. Records posts. Streams a list of events per URL. */
class FakeHttpDataSource(var answers: Map<String, String> = emptyMap(), var streams: Map<String, List<String>> = emptyMap()) : HttpDataSource {
    val posted = mutableListOf<Pair<String, String>>()
    val fetched = mutableListOf<String>()
    override suspend fun getText(url: String): String { fetched += url; return answers[url] ?: error("no answer from $url") }
    override suspend fun getBytes(url: String): ByteArray = getText(url).toByteArray()
    override suspend fun postJson(url: String, json: String) { posted += url to json }
    override fun events(url: String): Flow<String> = (streams[url] ?: error("no stream at $url")).asFlow()
}
