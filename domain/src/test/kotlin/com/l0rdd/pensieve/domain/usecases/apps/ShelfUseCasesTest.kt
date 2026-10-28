package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.fakes.FakeAppRepository
import com.l0rdd.pensieve.domain.fakes.FakeShelfRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfUseCasesTest {
    private val a = AppId("a"); private val b = AppId("b"); private val c = AppId("c")

    @Test
    fun `launching an app opens it and makes it no longer new`() = runTest {
        val apps = FakeAppRepository(); val shelf = FakeShelfRepository()
        LaunchApp(apps, shelf)(a)
        assertEquals(listOf(a), apps.launched)
        assertTrue(a in shelf.state.value.seen)
    }

    @Test
    fun `pinning adds at the end, pinning again takes it out`() = runTest {
        val shelf = FakeShelfRepository(Shelf(listOf(a), emptySet(), emptySet()))
        PinFavorite(shelf)(b)
        assertEquals(listOf(a, b), shelf.state.value.favorites)
        PinFavorite(shelf)(a)
        assertEquals(listOf(b), shelf.state.value.favorites)
    }

    @Test
    fun `a favourite moves one place and stops at the ends`() = runTest {
        val shelf = FakeShelfRepository(Shelf(listOf(a, b, c), emptySet(), emptySet()))
        MoveFavorite(shelf)(MoveFavorite.Param(c, -1))
        assertEquals(listOf(a, c, b), shelf.state.value.favorites)
        MoveFavorite(shelf)(MoveFavorite.Param(a, -1))
        assertEquals(listOf(a, c, b), shelf.state.value.favorites)
    }

    @Test
    fun `hiding takes it out of the favourites too`() = runTest {
        val shelf = FakeShelfRepository(Shelf(listOf(a, b), emptySet(), emptySet()))
        HideApp(shelf)(a)
        assertEquals(listOf(b), shelf.state.value.favorites)
        assertTrue(a in shelf.state.value.hidden)
    }
}
