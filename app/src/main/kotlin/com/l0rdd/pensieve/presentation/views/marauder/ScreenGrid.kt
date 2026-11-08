package com.l0rdd.pensieve.presentation.views.marauder

import android.view.KeyEvent
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.Screen
import com.l0rdd.pensieve.domain.entities.house.ScreenMove
import kotlin.math.roundToInt

/** An arrow of the remote. */
enum class Arrow { LEFT, RIGHT, UP, DOWN }

/** The arrow [keyCode] is, if it is one. */
fun arrowOf(keyCode: Int): Arrow? = when (keyCode) {
    KeyEvent.KEYCODE_DPAD_LEFT -> Arrow.LEFT
    KeyEvent.KEYCODE_DPAD_RIGHT -> Arrow.RIGHT
    KeyEvent.KEYCODE_DPAD_UP -> Arrow.UP
    KeyEvent.KEYCODE_DPAD_DOWN -> Arrow.DOWN
    else -> null
}

/** What the panel does with a key: fold on BACK let go, swallow it, move the focus on an arrow, or let the button have it. */
enum class PanelKey { CLOSE, SWALLOW, ARROW, PASS }

/** What the panel does with [keyCode] going [action]; while the spell runs ([spelling]) only BACK counts. */
fun panelKey(keyCode: Int, action: Int, spelling: Boolean): PanelKey = when {
    keyCode == KeyEvent.KEYCODE_BACK -> if (action == KeyEvent.ACTION_UP) PanelKey.CLOSE else PanelKey.SWALLOW
    spelling -> PanelKey.SWALLOW
    arrowOf(keyCode) != null -> PanelKey.ARROW
    else -> PanelKey.PASS
}

/** A button of the panel: its [row] and its [column] in it. */
data class Cell(val row: Int, val column: Int)

/**
 * The panel's buttons, [rows] of them (auto ▲ ■ ▼, by hand ▲ ▼, "it is" = rolled up = open): the
 * arrows go along a row and stay in it, and up or down to the button of the next row in the same
 * place along it (■ goes down to by hand ▼, by hand ▼ up to ▼).
 */
class ScreenGrid(private val rows: List<Int> = listOf(3, 2, 2)) {
    /** Where the focus starts: auto ▼. */
    val first = Cell(0, rows[0] - 1)

    /** Where [arrow] takes the focus from [at]. */
    fun move(at: Cell, arrow: Arrow): Cell = when (arrow) {
        Arrow.LEFT -> cell(at.row, at.column - 1)
        Arrow.RIGHT -> cell(at.row, at.column + 1)
        Arrow.UP -> across(at, at.row - 1)
        Arrow.DOWN -> across(at, at.row + 1)
    }

    private fun across(at: Cell, row: Int): Cell {
        val to = row.coerceIn(0, rows.lastIndex)
        return cell(to, (at.column * (rows[to] - 1f) / maxOf(1, rows[at.row] - 1)).roundToInt())
    }

    private fun cell(row: Int, column: Int) = Cell(row, column.coerceIn(0, rows[row] - 1))
}

/** What the panel's header says of the screen, as a string resource and its numbers: out of reach, rolled up, open, stopped somewhere, or on its way; null without a screen. */
fun gazetteWords(house: House): Pair<Int, List<Int>>? {
    val screen = house.screen ?: return null
    return when {
        !house.reachable -> R.string.mm_screen_unreachable to emptyList()
        screen.moving != null -> movingWords(screen)
        else -> restingWords(screen.position)
    }
}

private fun movingWords(screen: Screen): Pair<Int, List<Int>> {
    val down = screen.moving == ScreenMove.DOWN
    val target = screen.target ?: return (if (down) R.string.mm_screen_lowering else R.string.mm_screen_raising) to emptyList()
    return (if (down) R.string.mm_screen_lowering_to else R.string.mm_screen_raising_to) to listOf(target)
}

private fun restingWords(position: Int): Pair<Int, List<Int>> = when {
    position <= Screen.UP -> R.string.screen_rolled to emptyList()
    position >= Screen.DOWN -> R.string.screen_opened to emptyList()
    else -> R.string.mm_screen_at to listOf(position)
}
