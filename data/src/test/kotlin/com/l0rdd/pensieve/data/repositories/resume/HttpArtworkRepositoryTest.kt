package com.l0rdd.pensieve.data.repositories.resume

import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpArtworkRepositoryTest {
    @Test
    fun `jellyfin paths are asked of jellyfin at the needed size, once`() = runTest {
        val url = "http://jf:8096/Items/abc/Images/Primary?maxHeight=720&quality=85"
        val http = FakeHttpDataSource(mapOf(url to "png"))
        val repo = HttpArtworkRepository(http, "http://jf:8096")
        repo.load("/Items/abc/Images/Primary"); repo.load("/Items/abc/Images/Primary")
        assertEquals(listOf(url), http.fetched)
    }
}
