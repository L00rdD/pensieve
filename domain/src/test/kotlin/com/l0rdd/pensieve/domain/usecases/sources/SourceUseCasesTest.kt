package com.l0rdd.pensieve.domain.usecases.sources

import com.l0rdd.pensieve.domain.entities.sources.Source
import com.l0rdd.pensieve.domain.repositories.SourceRepository
import com.l0rdd.pensieve.domain.usecases.NoParam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SourceUseCasesTest {
    private val ps5 = Source("hdmi/HW4", "PS5", lit = false)

    private class Sources(private val emitted: List<List<Source>>) : SourceRepository {
        val opened = mutableListOf<String>()
        override fun observeSources(): Flow<List<Source>> = flowOf(*emitted.toTypedArray())
        override suspend fun open(id: String) { opened += id }
    }

    @Test
    fun `the sources, once per change`() = runTest {
        val lit = ps5.copy(lit = true)
        val seen = ObserveSources(Sources(listOf(listOf(ps5), listOf(ps5), listOf(lit)))).invoke(NoParam).toList()
        assertEquals(listOf(listOf(ps5), listOf(lit)), seen)
    }

    @Test
    fun `a source opens even before it is lit`() = runTest {
        val sources = Sources(emptyList())
        OpenSource(sources)(ps5)
        assertEquals(listOf("hdmi/HW4"), sources.opened)
    }
}
