package com.l0rdd.pensieve.data.repositories.resume

import com.l0rdd.pensieve.data.fakes.FakeAppsDataSource
import com.l0rdd.pensieve.data.fakes.FakeFeedDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedResumeRepositoryTest {
    private val body = """{"resume":[{"id":"abc","title":"Dune","runtime":9960,"position":4324},{"id":"zzz","title":"Other","runtime":10,"position":1}]}"""

    @Test
    fun `the most recent item, from each document the house pushes, a bad one skipped`() = runTest {
        val feed = FakeFeedDataSource(body, replay = 8)
        val repo = FeedResumeRepository(feed, FakeAppsDataSource(emptyList()), FeedResumeMapper())
        assertEquals("Dune", repo.observeResume().first()?.title)
        feed.push("not json")
        feed.push("""{"resume":[]}""")
        assertEquals(listOf("Dune", null), repo.observeResume().take(2).toList().map { it?.title })
        assertEquals(4324.seconds, FeedResumeMapper().toResume(com.l0rdd.pensieve.data.models.FeedResumeModel.parseList(body).first()).position)
    }
}
