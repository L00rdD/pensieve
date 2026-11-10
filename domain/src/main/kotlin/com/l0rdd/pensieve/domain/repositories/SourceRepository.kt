package com.l0rdd.pensieve.domain.repositories

import com.l0rdd.pensieve.domain.entities.sources.Source
import kotlinx.coroutines.flow.Flow

/** The projector's picture sources, and switching to one. */
interface SourceRepository {
    /** Every source, again whenever one is plugged, unplugged, woken up or put to sleep. */
    fun observeSources(): Flow<List<Source>>

    suspend fun open(id: String)
}
