package com.l0rdd.pensieve.presentation.views.common

import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House

/** What the screen's tile says of it, as a string resource: out of reach, on its way, down or up; null without a screen. */
fun screenState(house: House): Int? {
    val screen = house.screen ?: return null
    return when {
        !house.reachable -> R.string.unreachable
        screen.moving != null -> R.string.screen_moving
        screen.isDown -> R.string.screen_down
        else -> R.string.screen_up
    }
}
