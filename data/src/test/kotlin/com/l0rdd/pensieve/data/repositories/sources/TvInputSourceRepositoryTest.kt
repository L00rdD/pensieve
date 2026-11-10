package com.l0rdd.pensieve.data.repositories.sources

import com.l0rdd.pensieve.data.datasources.TvInputsDataSource
import com.l0rdd.pensieve.data.models.TvInputModel
import com.l0rdd.pensieve.domain.entities.sources.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TvInputSourceRepositoryTest {
    private class Inputs(private val inputs: List<TvInputModel>) : TvInputsDataSource {
        val played = mutableListOf<String>()
        override fun query() = inputs
        override fun changes(): Flow<Unit> = flowOf(Unit)
        override fun play(id: String): Boolean { played += id; return true }
    }

    private val hdmi1 = TvInputModel("hdmi/HW4", "PS5", hdmi = true, hidden = false, state = TvInputModel.CONNECTED)
    private val hdmi2 = TvInputModel("hdmi/HW5", "HDMI 2", hdmi = true, hidden = false, state = 1)
    private val hidden = TvInputModel("hdmi/HW6", "HDMI 3", hdmi = true, hidden = true, state = TvInputModel.CONNECTED)
    private val vlc = TvInputModel("org.videolan.vlc/.PreviewVideoInputService", "VLC", hdmi = false, hidden = false, state = TvInputModel.CONNECTED)

    @Test
    fun `the HDMI ports shown, lit when connected, the hidden ones and the apps' inputs left out`() = runTest {
        val sources = TvInputSourceRepository(Inputs(listOf(hdmi1, vlc, hdmi2, hidden))).observeSources().first()
        assertEquals(listOf(Source("hdmi/HW4", "PS5", lit = true), Source("hdmi/HW5", "HDMI 2", lit = false)), sources)
    }

    @Test
    fun `opening a source plays its input`() = runTest {
        val inputs = Inputs(listOf(hdmi1))
        TvInputSourceRepository(inputs).open("hdmi/HW4")
        assertEquals(listOf("hdmi/HW4"), inputs.played)
    }
}
