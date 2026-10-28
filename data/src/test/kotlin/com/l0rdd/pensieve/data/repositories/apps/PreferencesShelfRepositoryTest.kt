package com.l0rdd.pensieve.data.repositories.apps

import com.l0rdd.pensieve.data.fakes.FakeAppsDataSource
import com.l0rdd.pensieve.data.fakes.FakePreferencesDataSource
import com.l0rdd.pensieve.domain.entities.apps.AppId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesShelfRepositoryTest {
    @Test
    fun `on the first run the installed defaults become favourites and nothing is new`() = runTest {
        val prefs = FakePreferencesDataSource()
        val shelf = PreferencesShelfRepository(prefs, FakeAppsDataSource(listOf("netflix", "vlc")), listOf("jellyfin", "netflix")).observeShelf().first()
        assertEquals(listOf(AppId("netflix")), shelf.favorites)
        assertEquals(setOf(AppId("netflix"), AppId("vlc")), shelf.seen)
        assertEquals(true, prefs.values.containsKey("shelf"))
    }

    @Test
    fun `afterwards the stored shelf is read back`() = runTest {
        val prefs = FakePreferencesDataSource()
        PreferencesShelfRepository(prefs, FakeAppsDataSource(listOf("a", "b")), listOf("b")).observeShelf().first()
        val again = PreferencesShelfRepository(prefs, FakeAppsDataSource(listOf("a", "b", "new")), listOf("a")).observeShelf().first()
        assertEquals(listOf(AppId("b")), again.favorites)
        assertEquals(false, AppId("new") in again.seen)
    }
}
