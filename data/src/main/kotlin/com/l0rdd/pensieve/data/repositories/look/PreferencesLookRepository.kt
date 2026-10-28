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

/** The look picked on the projector; J.A.R.V.I.S. until one is picked. */
class PreferencesLookRepository(private val prefs: PreferencesDataSource) : LookRepository {
    private val state = MutableStateFlow<Look?>(null)

    override fun observeLook(): Flow<Look> = flow {
        if (state.value == null) {
            val stored = withContext(Dispatchers.IO) { prefs.read(KEY) }
            state.compareAndSet(null, Look.entries.firstOrNull { it.name == stored } ?: Look.JARVIS)
        }
        emitAll(state.filterNotNull())
    }

    override suspend fun save(look: Look) {
        state.value = look
        withContext(Dispatchers.IO) { prefs.write(KEY, look.name) }
    }

    private companion object {
        const val KEY = "look"
    }
}
