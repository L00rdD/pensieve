package com.l0rdd.pensieve.data.repositories.house

import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import com.l0rdd.pensieve.data.mappers.HubStatusMapper
import com.l0rdd.pensieve.domain.entities.house.OutletId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HubHouseRepositoryTest {
    private val hub = "http://hub"
    private val status = """{"outlets":[{"id":"basketball-neon","name":"Néon","on":true}],"covers":[{"id":"screen","name":"Écran","position":0}]}"""
    private fun repository(http: FakeHttpDataSource) = HubHouseRepository(http, HubStatusMapper(setOf("basketball-neon"), "screen"), hub, "screen")

    @Test
    fun `reads the house from the hub`() = runTest {
        val house = repository(FakeHttpDataSource(mapOf("$hub/api/status.json" to status))).observeHouse().first()
        assertEquals("Néon", house.outlets.single().name)
        assertEquals(0, house.screen?.position)
    }

    @Test
    fun `a hub that does not answer gives an unreachable house`() = runTest {
        assertFalse(repository(FakeHttpDataSource()).observeHouse().first().reachable)
    }

    @Test
    fun `commands go to the hub's API as JSON`() = runTest {
        val http = FakeHttpDataSource()
        val repo = repository(http)
        repo.switchOutlet(OutletId("basketball-neon"), false)
        repo.moveScreen(100)
        assertEquals(listOf("$hub/api/outlets/basketball-neon" to """{"on": false}""", "$hub/api/covers/screen" to """{"position": 100}"""), http.posted)
    }
}
