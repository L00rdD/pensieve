package com.l0rdd.pensieve.presentation.views.common

import android.view.KeyEvent
import android.view.View
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand

/** OK on the remote: the pad's centre, or Enter. */
fun isOk(keyCode: Int): Boolean =
    keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER

/** What OK held on a by-hand button sends: [command] when it goes down, STOP when it is let go, nothing while it repeats. */
fun heldCommand(action: Int, repeatCount: Int, command: ScreenCommand): ScreenCommand? = when {
    action == KeyEvent.ACTION_DOWN && repeatCount == 0 -> command
    action == KeyEvent.ACTION_UP -> ScreenCommand.STOP
    else -> null
}

/** Makes this button send [command] while OK is held on it, and stop the screen when it is let go. */
fun View.movesScreenWhileHeld(command: ScreenCommand, onCommand: (ScreenCommand) -> Unit) {
    setOnKeyListener { _, keyCode, event ->
        if (isOk(keyCode)) heldCommand(event.action, event.repeatCount, command)?.let(onCommand)
        isOk(keyCode)
    }
}
