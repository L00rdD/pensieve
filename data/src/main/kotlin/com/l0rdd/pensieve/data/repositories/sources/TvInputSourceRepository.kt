package com.l0rdd.pensieve.data.repositories.sources

import com.l0rdd.pensieve.data.datasources.TvInputsDataSource
import com.l0rdd.pensieve.data.models.TvInputModel
import com.l0rdd.pensieve.domain.entities.sources.Source
import com.l0rdd.pensieve.domain.repositories.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/** The projector's HDMI ports, as Android's TV inputs tell them; the hidden ones left out. */
class TvInputSourceRepository(private val inputs: TvInputsDataSource) : SourceRepository {

    override fun observeSources(): Flow<List<Source>> = inputs.changes().map {
        inputs.query().filter { it.hdmi && !it.hidden }.map { Source(it.id, it.label, it.state == TvInputModel.CONNECTED) }
    }.flowOn(Dispatchers.IO)

    override suspend fun open(id: String) { inputs.play(id) }
}
