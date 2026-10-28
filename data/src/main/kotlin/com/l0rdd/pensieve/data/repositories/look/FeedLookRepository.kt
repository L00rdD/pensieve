package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.datasources.FeedDataSource
import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.data.datasources.PreferencesDataSource
import com.l0rdd.pensieve.data.models.FeedSettingsModel
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.repositories.LookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The look, shared with the rest of the house: the one the feed's settings name wins when set,
 * as it does on Errol, the instant the house pushes it; else the one picked here, kept in the
 * preferences (J.A.R.V.I.S. until one is picked). Picking one posts it to the feed's settings
 * ([settingsUrl]) so Errol and the hub change with it, and stops the hub from following its
 * own page. A feed that does not answer leaves the look as it was.
 */
class FeedLookRepository(
    private val feed: FeedDataSource,
    private val http: HttpDataSource,
    private val prefs: PreferencesDataSource,
    private val settingsUrl: String,
    private val firstWordMs: Long = 2_000,
) : LookRepository {
    private val own = MutableStateFlow<Look?>(null)
    private val shared = MutableStateFlow<Look?>(null)

    override fun observeLook(): Flow<Look> = flow {
        if (own.value == null) {
            val stored = withContext(Dispatchers.IO) { prefs.read(KEY) }
            own.compareAndSet(null, Look.fromId(stored) ?: Look.JARVIS)
        }
        // the house's word first, when it comes quickly; the look picked here otherwise
        withTimeoutOrNull(firstWordMs) { feed.observe().first() }?.let(::read)
        coroutineScope {
            launch { feed.observe().collect(::read) }
            emitAll(combine(shared, own.filterNotNull()) { house, mine -> house ?: mine })
        }
    }

    override suspend fun save(look: Look) {
        own.value = look
        shared.value = look
        withContext(Dispatchers.IO) { prefs.write(KEY, look.id) }
        runCatching { http.postJson(settingsUrl, """{"look":"${look.id}","follow_hub":false}""") }
    }

    /** The feed's look; a document that cannot be read changes nothing. */
    private fun read(text: String) {
        runCatching { Look.fromId(FeedSettingsModel.parse(text).look) }.onSuccess { shared.value = it }
    }

    private companion object {
        const val KEY = "look"
    }
}
