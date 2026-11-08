package com.l0rdd.pensieve.presentation.views.common

import android.view.View
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand

/**
 * The screen's panel a look draws itself, laid in its own column in place of what it covers (the
 * Marauder's comes out of the parchment where the house's list was), instead of over the page.
 */
interface ScreenControl {
    val view: View

    /** Shows where the screen is now. */
    fun show(house: House)

    /** Comes into sight, then has the focus on ▼; it keeps it until it goes. */
    fun reveal()

    /** Goes out of sight, then [then]. */
    fun vanish(then: () -> Unit)
}

/** A home view that can lay a look's screen panel in its own layout. */
interface ScreenHost {
    /**
     * Lays the look's screen panel in place and reveals it; null when the look has none of its
     * own (the panel then unfolds over the page, [ScreenPanel]). [onCommand] moves the screen,
     * [onClose] is told when BACK is pressed on it.
     */
    fun openScreen(onCommand: (ScreenCommand) -> Unit, onClose: () -> Unit): ScreenControl?

    /** Makes the open panel vanish and puts back what it covered, then [then]. */
    fun closeScreen(then: () -> Unit)
}

/** Words written on the home's hint line for a moment, as by a quill (the Marauder's oath and its "mischief managed"). */
fun interface Saying {
    fun say(words: String)
}
