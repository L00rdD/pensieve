package com.l0rdd.pensieve.data.repositories.projector

import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import com.l0rdd.pensieve.domain.entities.apps.AppId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HubProjectorRepositoryTest {
    private val hub = "http://hub"

    @Test
    fun `the apps to close go to the hub in one order`() = runTest {
        val http = FakeHttpDataSource()
        HubProjectorRepository(http, hub, "projector").close(listOf(AppId("com.netflix.ninja"), AppId("org.videolan.vlc")))
        assertEquals(listOf("$hub/api/projectors/projector" to """{"close": ["com.netflix.ninja", "org.videolan.vlc"]}"""), http.posted)
    }

    @Test
    fun `no app, no order`() = runTest {
        val http = FakeHttpDataSource()
        HubProjectorRepository(http, hub, "projector").close(emptyList())
        assertEquals(emptyList<Pair<String, String>>(), http.posted)
    }
}
