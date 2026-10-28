package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.AppKind
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.fakes.FakeAppRepository
import com.l0rdd.pensieve.domain.fakes.FakeShelfRepository
import com.l0rdd.pensieve.domain.fakes.app
import com.l0rdd.pensieve.domain.usecases.NoParam
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveHomeAppsTest {
    private val apps = FakeAppRepository(listOf(app("tv.vlc"), app("com.netflix"), app("store.play", AppKind.STORE), app("org.jellyfin"), app("tv.zebra")))

    @Test
    fun `favourites keep the user's order and the others go by name, the store last`() = runTest {
        val shelf = FakeShelfRepository(Shelf(listOf(AppId("org.jellyfin"), AppId("com.netflix")), emptySet(), emptySet()))
        val home = ObserveHomeApps(apps, shelf)(NoParam).first()
        assertEquals(listOf("org.jellyfin", "com.netflix"), home.favorites.map { it.app.id.value })
        assertEquals(listOf("tv.vlc", "tv.zebra", "store.play"), home.others.map { it.app.id.value })
    }

    @Test
    fun `hidden apps are left out, and a favourite no longer installed is skipped`() = runTest {
        val shelf = FakeShelfRepository(Shelf(listOf(AppId("gone.app"), AppId("com.netflix")), setOf(AppId("tv.zebra")), emptySet()))
        val home = ObserveHomeApps(apps, shelf)(NoParam).first()
        assertEquals(listOf("com.netflix"), home.favorites.map { it.app.id.value })
        assertEquals(false, home.others.any { it.app.id.value == "tv.zebra" })
    }

    @Test
    fun `an app never seen is new, but the store never is`() = runTest {
        val shelf = FakeShelfRepository(Shelf(emptyList(), emptySet(), setOf(AppId("tv.vlc"), AppId("com.netflix"), AppId("org.jellyfin"))))
        val news = ObserveHomeApps(apps, shelf)(NoParam).first().others.filter { it.isNew }.map { it.app.id.value }
        assertEquals(listOf("tv.zebra"), news)
    }
}
