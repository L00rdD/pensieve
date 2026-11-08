package com.l0rdd.pensieve.presentation.views.common

import android.view.KeyEvent
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenKeysTest {
    @Test
    fun `OK is the pad's centre or Enter`() {
        assertTrue(isOk(KeyEvent.KEYCODE_DPAD_CENTER))
        assertTrue(isOk(KeyEvent.KEYCODE_ENTER))
        assertTrue(isOk(KeyEvent.KEYCODE_NUMPAD_ENTER))
        assertFalse(isOk(KeyEvent.KEYCODE_DPAD_UP))
    }

    @Test
    fun `OK held sends its command once when pressed, and stops the screen when let go`() {
        assertEquals(ScreenCommand.PRESS_UP, heldCommand(KeyEvent.ACTION_DOWN, 0, ScreenCommand.PRESS_UP))
        assertEquals(null, heldCommand(KeyEvent.ACTION_DOWN, 3, ScreenCommand.PRESS_UP))
        assertEquals(ScreenCommand.STOP, heldCommand(KeyEvent.ACTION_UP, 0, ScreenCommand.PRESS_DOWN))
    }
}
