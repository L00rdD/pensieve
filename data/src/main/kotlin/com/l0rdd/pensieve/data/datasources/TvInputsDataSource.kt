package com.l0rdd.pensieve.data.datasources

import com.l0rdd.pensieve.data.models.TvInputModel
import kotlinx.coroutines.flow.Flow

/** Android's TV inputs (the HDMI ports, among others), and playing one. */
interface TvInputsDataSource {
    fun query(): List<TvInputModel>

    /** Fires once now, then whenever an input is added, removed, updated or changes state. */
    fun changes(): Flow<Unit>

    /** Plays the input [id] full screen, through the system's passthrough player; false when nothing answers. */
    fun play(id: String): Boolean
}
