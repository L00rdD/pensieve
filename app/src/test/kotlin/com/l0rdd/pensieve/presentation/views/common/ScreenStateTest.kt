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
}
