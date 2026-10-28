package com.l0rdd.pensieve.presentation

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.l0rdd.pensieve.PensieveApplication
import com.l0rdd.pensieve.dependency_injector.injectSettingsKey
import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.domain.usecases.system.OpenSystemScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The remote's ⚙ key, in any app. SystemUI only hands that key to the Google TV home (disabled
 * here), so this service, which sees keys before the system does, opens the quick panel. While the
 * panel is in front the key reaches it, and closes it.
 * Every other key goes through untouched.
 */
class SettingsKeyService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var openSystemScreen: OpenSystemScreen

    override fun onServiceConnected() {
        openSystemScreen = injectSettingsKey((application as PensieveApplication).scope)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // the JMGO remote's ⚙ sends KEYCODE_NOTIFICATION; a standard remote sends KEYCODE_SETTINGS
        if (event.keyCode != KeyEvent.KEYCODE_NOTIFICATION && event.keyCode != KeyEvent.KEYCODE_SETTINGS) return false
        if (event.action == KeyEvent.ACTION_UP) scope.launch { runCatching { openSystemScreen(SystemScreen.QUICK_PANEL) } }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
