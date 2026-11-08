package com.l0rdd.pensieve.domain.usecases.house

import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import com.l0rdd.pensieve.domain.fakes.FakeHouseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandScreenTest {

    @Test
    fun `raises, stops and lowers the screen`() = runTest {
        val hub = FakeHouseRepository()
        CommandScreen(hub).run { invoke(ScreenCommand.RAISE); invoke(ScreenCommand.STOP); invoke(ScreenCommand.LOWER) }
        assertEquals(listOf(Screen.UP, Screen.DOWN), hub.moved)
        assertEquals(1, hub.stopped)
        assertEquals(emptyList<Int>(), hub.declared)
    }

    @Test
    fun `the remote's arrows are pushed whatever the hub reckons`() = runTest {
        val hub = FakeHouseRepository()
        CommandScreen(hub).run { invoke(ScreenCommand.PRESS_DOWN); invoke(ScreenCommand.STOP); invoke(ScreenCommand.PRESS_UP) }
        assertEquals(listOf(ScreenMove.DOWN, ScreenMove.UP), hub.pressed)
        assertEquals(1, hub.stopped)
        assertEquals(emptyList<Int>(), hub.moved)
    }

    @Test
    fun `rolled up and open tell the hub where the screen is, nothing moves`() = runTest {
        val hub = FakeHouseRepository()
        CommandScreen(hub).run { invoke(ScreenCommand.ROLLED_UP); invoke(ScreenCommand.OPEN) }
        assertEquals(listOf(Screen.UP, Screen.DOWN), hub.declared)
        assertEquals(emptyList<Int>(), hub.moved)
    }
}
