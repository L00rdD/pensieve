package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.data.datasources.PreferencesDataSource
import com.l0rdd.pensieve.data.models.FeedSettingsModel
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.repositories.LookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The look, shared with the rest of the house: the one the feed's settings name ([feedUrl],
 * read every [pollMs]) wins when set, as it does on Errol; else the one picked here, kept in
 * the preferences (J.A.R.V.I.S. until one is picked). Picking one posts it to the feed's
 * settings ([settingsUrl]) so Errol and the hub change with it, and stops the hub from
 * following its own page. A feed that does not answer leaves the look as it was.
 */
class FeedLookRepository(
    private val http: HttpDataSource,
    private val prefs: PreferencesDataSource,
    private val feedUrl: String,
    private val settingsUrl: String,
    private val pollMs: Long = 3_000,
) : LookRepository {
    private val own = MutableStateFlow<Look?>(null)
    private val shared = MutableStateFlow<Look?>(null)

    override fun observeLook(): Flow<Look> = flow {
        if (own.value == null) {
            val stored = withContext(Dispatchers.IO) { prefs.read(KEY) }
            own.compareAndSet(null, Look.fromId(stored) ?: Look.JARVIS)
        }
        read()
        coroutineScope {
            launch { while (true) { delay(pollMs); read() } }
            emitAll(combine(shared, own.filterNotNull()) { feed, mine -> feed ?: mine })
        }
    }

    override suspend fun save(look: Look) {
        own.value = look
        shared.value = look
        withContext(Dispatchers.IO) { prefs.write(KEY, look.id) }
        runCatching { http.postJson(settingsUrl, """{"look":"${look.id}","follow_hub":false}""") }
    }

    /** The feed's look; a feed that does not answer changes nothing. */
    private suspend fun read() {
        runCatching { Look.fromId(FeedSettingsModel.parse(http.getText(feedUrl)).look) }
            .onSuccess { shared.value = it }
    }

    private companion object {
        const val KEY = "look"
    }
}
