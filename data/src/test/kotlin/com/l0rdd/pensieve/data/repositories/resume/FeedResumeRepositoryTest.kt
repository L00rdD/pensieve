package com.l0rdd.pensieve.data.repositories.resume

import com.l0rdd.pensieve.data.fakes.FakeAppsDataSource
import com.l0rdd.pensieve.data.fakes.FakeHttpDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedResumeRepositoryTest {
    private val feed = "http://media/screen.json"
    private val body = """{"resume":[{"id":"abc","title":"Dune","runtime":9960,"position":4324},{"id":"zzz","title":"Other","runtime":10,"position":1}]}"""

    @Test
    fun `the most recent item, and the last answer kept when the feed goes quiet`() = runTest {
        val http = FakeHttpDataSource(mapOf(feed to body))
        val repo = FeedResumeRepository(http, FakeAppsDataSource(emptyList()), FeedResumeMapper(), feed, pollMs = 1)
        val seen = mutableListOf<String?>()
        repo.observeResume().take(1).toList().forEach { seen += it?.title }
        http.answers = emptyMap()
        repo.observeResume().take(1).toList().forEach { seen += it?.title }
        assertEquals("Dune", seen[0])
        assertEquals(4324.seconds, FeedResumeMapper().toResume(com.l0rdd.pensieve.data.models.FeedResumeModel.parseList(body).first()).position)
    }
}
