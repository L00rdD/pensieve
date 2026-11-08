package com.l0rdd.pensieve.presentation.views.common

import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenStateTest {
    private fun house(position: Int, moving: ScreenMove? = null, reachable: Boolean = true) =
        House(emptyList(), Screen("screen", "Écran", position, moving), reachable)

    @Test
    fun `the screen is up, down or on its way`() {
        assertEquals(R.string.screen_up, screenState(house(Screen.UP)))
        assertEquals(R.string.screen_down, screenState(house(Screen.DOWN)))
        assertEquals(R.string.screen_moving, screenState(house(Screen.UP, ScreenMove.DOWN)))
    }

    @Test
    fun `a hub out of reach says so, and a house without a screen says nothing`() {
        assertEquals(R.string.unreachable, screenState(house(Screen.DOWN, reachable = false)))
        assertEquals(null, screenState(House(emptyList(), null, reachable = true)))
    }

    @Test
    fun `a screen at rest is rolled up, open, or somewhere between`() {
        assertEquals(R.string.screen_rolled to emptyList<Int>(), screenWords(Screen("screen", "Écran", 0, null)))
        assertEquals(R.string.screen_opened to emptyList<Int>(), screenWords(Screen("screen", "Écran", 100, null)))
        assertEquals(R.string.screen_at to listOf(37), screenWords(Screen("screen", "Écran", 37, null)))
    }

    @Test
    fun `a moving screen tells its way, and where it is going when the hub knows`() {
        assertEquals(R.string.screen_lowering_to to listOf(48, 100), screenWords(Screen("screen", "Écran", 48, ScreenMove.DOWN, 100)))
        assertEquals(R.string.screen_raising_to to listOf(48, 0), screenWords(Screen("screen", "Écran", 48, ScreenMove.UP, 0)))
        assertEquals(R.string.screen_lowering to listOf(20), screenWords(Screen("screen", "Écran", 20, ScreenMove.DOWN)))
        assertEquals(R.string.screen_raising to listOf(20), screenWords(Screen("screen", "Écran", 20, ScreenMove.UP)))
    }
}
