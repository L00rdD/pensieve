package com.l0rdd.pensieve.presentation

import android.view.KeyEvent
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.state.home.Arrange
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeMenu
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeKeysTest {
    private val down = KeyEvent.ACTION_DOWN
    private val up = KeyEvent.ACTION_UP

    @Test
    fun `BACK does nothing on the home screen, and goes to the menu when one is open`() {
        assertEquals(RemoteKey.SWALLOW, remoteKey(KeyEvent.KEYCODE_BACK, up, menuOpen = false))
        assertEquals(RemoteKey.SWALLOW, remoteKey(KeyEvent.KEYCODE_BACK, down, menuOpen = false))
        assertEquals(RemoteKey.PASS, remoteKey(KeyEvent.KEYCODE_BACK, up, menuOpen = true))
    }

    @Test
    fun `the settings key opens the settings when released`() {
        assertEquals(RemoteKey.SWALLOW, remoteKey(KeyEvent.KEYCODE_SETTINGS, down, menuOpen = false))
        assertEquals(RemoteKey.SETTINGS, remoteKey(KeyEvent.KEYCODE_SETTINGS, up, menuOpen = false))
        assertEquals(RemoteKey.SETTINGS, remoteKey(KeyEvent.KEYCODE_SETTINGS, up, menuOpen = true))
    }

    @Test
    fun `the menu key opens the focused tile's menu when released, unless a menu is open`() {
        assertEquals(RemoteKey.SWALLOW, remoteKey(KeyEvent.KEYCODE_MENU, down, menuOpen = false))
        assertEquals(RemoteKey.MENU, remoteKey(KeyEvent.KEYCODE_MENU, up, menuOpen = false))
        assertEquals(RemoteKey.SWALLOW, remoteKey(KeyEvent.KEYCODE_MENU, up, menuOpen = true))
    }

    @Test
    fun `the other keys go to the views`() {
        assertEquals(RemoteKey.PASS, remoteKey(KeyEvent.KEYCODE_DPAD_CENTER, up, menuOpen = false))
        assertEquals(RemoteKey.PASS, remoteKey(KeyEvent.KEYCODE_DPAD_LEFT, down, menuOpen = true))
    }

    @Test
    fun `a favourite can be unpinned, moved and hidden, another app pinned or hidden`() {
        val id = AppId("vlc")
        assertEquals(listOf(
            R.string.arrange_unpin to HomeEvent.Arranged(id, Arrange.PIN),
            R.string.arrange_earlier to HomeEvent.Arranged(id, Arrange.EARLIER),
            R.string.arrange_later to HomeEvent.Arranged(id, Arrange.LATER),
            R.string.arrange_hide to HomeEvent.Arranged(id, Arrange.HIDE),
            R.string.arrange_cancel to HomeEvent.MenuClosed,
        ), menuEntries(HomeMenu.ArrangeApp(id, "VLC", favorite = true)))
        assertEquals(listOf(
            R.string.arrange_pin to HomeEvent.Arranged(id, Arrange.PIN),
            R.string.arrange_hide to HomeEvent.Arranged(id, Arrange.HIDE),
            R.string.arrange_cancel to HomeEvent.MenuClosed,
        ), menuEntries(HomeMenu.ArrangeApp(id, "VLC", favorite = false)))
    }

    @Test
    fun `the look menu offers the three looks`() {
        assertEquals(listOf(
            R.string.look_jarvis to HomeEvent.LookPicked(Look.JARVIS),
            R.string.look_cyberpunk to HomeEvent.LookPicked(Look.CYBERPUNK),
            R.string.look_marauder to HomeEvent.LookPicked(Look.MARAUDER),
        ), menuEntries(HomeMenu.PickLook))
    }
}
