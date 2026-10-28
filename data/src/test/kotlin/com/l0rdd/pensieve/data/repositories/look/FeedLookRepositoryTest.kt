package com.l0rdd.pensieve.data.repositories.look

import com.l0rdd.pensieve.data.fakes.FakeFeedDataSource
import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import com.l0rdd.pensieve.data.fakes.FakePreferencesDataSource
import com.l0rdd.pensieve.domain.entities.look.Look
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedLookRepositoryTest {
    private val settings = "http://media/screen/settings"

    private fun repo(feed: FakeFeedDataSource, http: FakeHttpDataSource = FakeHttpDataSource(), prefs: FakePreferencesDataSource = FakePreferencesDataSource()) =
        FeedLookRepository(feed, http, prefs, settings, firstWordMs = 50)

    @Test
    fun `the house's look wins over the one picked here, and follows what it pushes`() = runTest {
        val feed = FakeFeedDataSource("""{"settings":{"look":"hogwarts","follow_hub":true}}""")
        val repo = repo(feed, prefs = FakePreferencesDataSource(mutableMapOf("look" to "friday")))
        assertEquals(Look.MARAUDER, repo.observeLook().first())
        feed.push("""{"settings":{"look":"cyberpunk"}}""")
        assertEquals(Look.CYBERPUNK, repo.observeLook().first())
    }

    @Test
    fun `without a look in the feed, or without a feed, the one picked here shows`() = runTest {
        val unset = FakeFeedDataSource("""{"settings":{"look":null,"follow_hub":false}}""")
        assertEquals(Look.CYBERPUNK, repo(unset, prefs = FakePreferencesDataSource(mutableMapOf("look" to "cyberpunk"))).observeLook().first())
        assertEquals(Look.JARVIS, repo(FakeFeedDataSource()).observeLook().first())
    }

    @Test
    fun `picking a look keeps it here and posts it to the house's settings`() = runTest {
        val http = FakeHttpDataSource()
        val prefs = FakePreferencesDataSource()
        val repo = repo(FakeFeedDataSource(), http, prefs)
        repo.save(Look.MARAUDER)
        assertEquals("hogwarts", prefs.values["look"])
        assertEquals(listOf(settings to """{"look":"hogwarts","follow_hub":false}"""), http.posted)
        assertEquals(Look.MARAUDER, repo.observeLook().first())
    }
}
