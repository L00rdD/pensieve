package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.fakes.FakePreferencesDataSource
import com.l0rdd.pensieve.domain.entities.look.Look
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesLookRepositoryTest {
    @Test
    fun `the look picked here shows, J A R V I S until one is picked`() = runTest {
        assertEquals(Look.CYBERPUNK, PreferencesLookRepository(FakePreferencesDataSource(mutableMapOf("look" to "cyberpunk"))).observeLook().first())
        assertEquals(Look.JARVIS, PreferencesLookRepository(FakePreferencesDataSource()).observeLook().first())
    }

    @Test
    fun `picking a look keeps it here only, and after a restart`() = runTest {
        val prefs = FakePreferencesDataSource()
        PreferencesLookRepository(prefs).save(Look.MARAUDER)
        assertEquals(mapOf("look" to "hogwarts"), prefs.values)
        assertEquals(Look.MARAUDER, PreferencesLookRepository(prefs).observeLook().first())
    }
}
