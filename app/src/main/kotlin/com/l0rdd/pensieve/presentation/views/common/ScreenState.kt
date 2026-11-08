package com.l0rdd.pensieve.presentation.views.common

import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove

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

/** Where the screen is, in words, as a string resource and its numbers: rolled up, open, at some %, or on its way. */
fun screenWords(screen: Screen): Pair<Int, List<Int>> {
    val down = screen.moving == ScreenMove.DOWN
    val target = screen.target
    return when {
        screen.moving == null -> restingWords(screen.position)
        target == null -> (if (down) R.string.screen_lowering else R.string.screen_raising) to listOf(screen.position)
        else -> (if (down) R.string.screen_lowering_to else R.string.screen_raising_to) to listOf(screen.position, target)
    }
}

private fun restingWords(position: Int): Pair<Int, List<Int>> = when {
    position <= Screen.UP -> R.string.screen_rolled to emptyList()
    position >= Screen.DOWN -> R.string.screen_opened to emptyList()
    else -> R.string.screen_at to listOf(position)
}
