package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.datasources.PreferencesDataSource
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.repositories.LookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * The look picked on the projector, kept in the preferences; J.A.R.V.I.S. until one is picked.
 * Pensieve's own: the house's look (the feed's settings, followed by Errol and the hub) neither
 * changes it nor is changed by it.
 */
class PreferencesLookRepository(private val prefs: PreferencesDataSource) : LookRepository {
    private val state = MutableStateFlow<Look?>(null)

    override fun observeLook(): Flow<Look> = flow {
        if (state.value == null) {
            val stored = withContext(Dispatchers.IO) { prefs.read(KEY) }
            state.compareAndSet(null, Look.fromId(stored) ?: Look.JARVIS)
        }
        emitAll(state.filterNotNull())
    }

    override suspend fun save(look: Look) {
        state.value = look
        withContext(Dispatchers.IO) { prefs.write(KEY, look.id) }
    }

    private companion object {
        const val KEY = "look"
    }
}
