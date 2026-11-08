package com.l0rdd.pensieve.presentation.views.marauder

import android.view.KeyEvent
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenGridTest {
    private val grid = ScreenGrid()

    @Test
    fun `the focus starts on auto down`() {
        assertEquals(Cell(0, 2), grid.first)
    }

    @Test
    fun `left and right go along a row and stay in it`() {
        assertEquals(Cell(0, 1), grid.move(Cell(0, 2), Arrow.LEFT))
        assertEquals(Cell(0, 0), grid.move(Cell(0, 0), Arrow.LEFT))
        assertEquals(Cell(1, 1), grid.move(Cell(1, 1), Arrow.RIGHT))
        assertEquals(Cell(2, 1), grid.move(Cell(2, 0), Arrow.RIGHT))
    }

    @Test
    fun `up and down go to the button in the same place along the next row, and stay in the grid`() {
        assertEquals(Cell(1, 0), grid.move(Cell(0, 0), Arrow.DOWN))
        assertEquals(Cell(1, 1), grid.move(Cell(0, 1), Arrow.DOWN))
        assertEquals(Cell(1, 1), grid.move(Cell(0, 2), Arrow.DOWN))
        assertEquals(Cell(0, 2), grid.move(Cell(1, 1), Arrow.UP))
        assertEquals(Cell(0, 0), grid.move(Cell(1, 0), Arrow.UP))
        assertEquals(Cell(2, 1), grid.move(Cell(1, 1), Arrow.DOWN))
        assertEquals(Cell(2, 0), grid.move(Cell(2, 0), Arrow.DOWN))
        assertEquals(Cell(0, 1), grid.move(Cell(0, 1), Arrow.UP))
    }

    @Test
    fun `the pad's arrows are arrows, nothing else is`() {
        assertEquals(Arrow.LEFT, arrowOf(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(Arrow.RIGHT, arrowOf(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(Arrow.UP, arrowOf(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(Arrow.DOWN, arrowOf(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(null, arrowOf(KeyEvent.KEYCODE_DPAD_CENTER))
    }

    @Test
    fun `BACK let go folds the panel, even while the spell runs, which swallows every other key`() {
        assertEquals(PanelKey.CLOSE, panelKey(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_UP, spelling = false))
        assertEquals(PanelKey.CLOSE, panelKey(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_UP, spelling = true))
        assertEquals(PanelKey.SWALLOW, panelKey(KeyEvent.KEYCODE_BACK, KeyEvent.ACTION_DOWN, spelling = false))
        assertEquals(PanelKey.SWALLOW, panelKey(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, spelling = true))
        assertEquals(PanelKey.SWALLOW, panelKey(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN, spelling = true))
    }

    @Test
    fun `the arrows move the focus, OK goes to the button`() {
        assertEquals(PanelKey.ARROW, panelKey(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.ACTION_DOWN, spelling = false))
        assertEquals(PanelKey.PASS, panelKey(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, spelling = false))
    }

    private fun house(position: Int, moving: ScreenMove? = null, target: Int? = null, reachable: Boolean = true) =
        House(emptyList(), Screen("screen", "Écran", position, moving, target), reachable)

    @Test
    fun `the header says the screen is rolled up, open, stopped somewhere, or out of reach`() {
        assertEquals(R.string.screen_rolled to emptyList<Int>(), gazetteWords(house(0)))
        assertEquals(R.string.screen_opened to emptyList<Int>(), gazetteWords(house(100)))
        assertEquals(R.string.mm_screen_at to listOf(40), gazetteWords(house(40)))
        assertEquals(R.string.mm_screen_unreachable to emptyList<Int>(), gazetteWords(house(40, ScreenMove.DOWN, reachable = false)))
        assertEquals(null, gazetteWords(House(emptyList(), null, reachable = true)))
    }

    @Test
    fun `the header says which way the screen goes, and where to when the hub knows`() {
        assertEquals(R.string.mm_screen_lowering_to to listOf(100), gazetteWords(house(40, ScreenMove.DOWN, 100)))
        assertEquals(R.string.mm_screen_raising_to to listOf(0), gazetteWords(house(40, ScreenMove.UP, 0)))
        assertEquals(R.string.mm_screen_lowering to emptyList<Int>(), gazetteWords(house(40, ScreenMove.DOWN)))
        assertEquals(R.string.mm_screen_raising to emptyList<Int>(), gazetteWords(house(40, ScreenMove.UP)))
    }
}
