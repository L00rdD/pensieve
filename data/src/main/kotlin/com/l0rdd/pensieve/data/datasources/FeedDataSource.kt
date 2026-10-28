package com.l0rdd.pensieve.data.datasources

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn

/** Errol's feed (screen.json), each version of the document as text, as the house writes it. */
interface FeedDataSource {
    fun observe(): Flow<String>
}

/**
 * The feed as it is pushed: the house's event stream ([eventsUrl], Server-Sent Events) hands
 * every version of screen.json the instant it changes. While the stream cannot be had (the
 * connection broke, an older house), the feed is read ([feedUrl]) every [pollMs] instead, and
 * the stream tried again. One connection serves everyone who observes, and a newcomer gets
 * the latest document at once.
 */
class StreamingFeedDataSource(
    private val http: HttpDataSource,
    private val feedUrl: String,
    private val eventsUrl: String,
    scope: CoroutineScope,
    private val pollMs: Long = 3_000,
) : FeedDataSource {
    private val documents: Flow<String> = flow {
        while (true) {
            try {
                http.events(eventsUrl).collect { emit(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // the stream is not to be had, or broke: read the feed the plain way meanwhile
            }
            try {
                emit(http.getText(feedUrl))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // the feed does not answer either: whoever observes keeps what they had
            }
            delay(pollMs)
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), replay = 1)

    override fun observe(): Flow<String> = documents
}
