package com.l0rdd.pensieve.presentation.state.panel

import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.look.Look

/** What the quick panel shows; [done] once an entry that leaves the panel has been picked. */
data class QuickPanelState(val look: Look?, val house: House, val done: Boolean) {
    companion object {
        val INITIAL = QuickPanelState(null, House.UNKNOWN, done = false)
    }
}
