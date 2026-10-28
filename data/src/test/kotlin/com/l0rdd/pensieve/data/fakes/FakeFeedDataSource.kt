package com.l0rdd.pensieve.data.fakes

import com.l0rdd.pensieve.data.datasources.FeedDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** The feed as pushed by hand: [push] a document and whoever observes gets it; a newcomer gets the last [replay] ones. */
class FakeFeedDataSource(vararg initial: String, replay: Int = 1) : FeedDataSource {
    private val documents = MutableSharedFlow<String>(replay = replay)
    init { initial.forEach { documents.tryEmit(it) } }
    fun push(text: String) { documents.tryEmit(text) }
    override fun observe(): Flow<String> = documents
}
