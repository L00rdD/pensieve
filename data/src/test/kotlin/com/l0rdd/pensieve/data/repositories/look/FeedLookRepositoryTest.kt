package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import com.l0rdd.pensieve.data.fakes.FakePreferencesDataSource
import com.l0rdd.pensieve.domain.entities.look.Look
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedLookRepositoryTest {
    private val feed = "http://media/screen.json"
    private val settings = "http://media/screen/settings"

    private fun repo(http: FakeHttpDataSource, prefs: FakePreferencesDataSource) =
        FeedLookRepository(http, prefs, feed, settings, pollMs = 1)

    @Test
    fun `the feed's look wins over the one picked here`() = runTest {
        val http = FakeHttpDataSource(mapOf(feed to """{"settings":{"look":"hogwarts","follow_hub":true}}"""))
        val prefs = FakePreferencesDataSource(mutableMapOf("look" to "friday"))
        assertEquals(Look.MARAUDER, repo(http, prefs).observeLook().first())
    }

    @Test
    fun `without a look in the feed, or without a feed, the one picked here shows`() = runTest {
        val unset = FakeHttpDataSource(mapOf(feed to """{"settings":{"look":null,"follow_hub":false}}"""))
        assertEquals(Look.CYBERPUNK, repo(unset, FakePreferencesDataSource(mutableMapOf("look" to "cyberpunk"))).observeLook().first())
        val silent = FakeHttpDataSource(emptyMap())
        assertEquals(Look.JARVIS, repo(silent, FakePreferencesDataSource()).observeLook().first())
    }

    @Test
    fun `picking a look keeps it here and posts it to the house's settings`() = runTest {
        val http = FakeHttpDataSource(mapOf(feed to """{"settings":{"look":"friday"}}"""))
        val prefs = FakePreferencesDataSource()
        val repo = repo(http, prefs)
        repo.save(Look.MARAUDER)
        assertEquals("hogwarts", prefs.values["look"])
        assertEquals(listOf(settings to """{"look":"hogwarts","follow_hub":false}"""), http.posted)
        // the feed has not written the change out yet: the look picked shows at once
        http.answers = emptyMap()
        assertEquals(Look.MARAUDER, repo.observeLook().first())
    }
}
