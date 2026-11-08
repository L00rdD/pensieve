package com.l0rdd.pensieve.presentation.views.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.R
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.ScreenCommand

/**
 * The screen's tile unfolded, in the look's menu style, over the tiles above it: where the screen
 * is (in words and on a gauge), then three rows: auto (▲ ■ ▼ all the way), by hand (▲ ▼ the
 * remote's way, as long as OK is held) and "it is" (= rolled up, = open: the hub is told, nothing
 * moves). The focus stays inside; BACK folds it.
 */
class ScreenPanel(
    context: Context,
    private val look: MenuStyle,
    private val onCommand: (ScreenCommand) -> Unit,
    private val onClose: () -> Unit,
) : FrameLayout(context) {
    private var firstFocus: View? = null
    private val state = text("", look.text, 22f)
    private val gauge = Gauge()
    private val hintLine = text("", look.text, 19f).apply { alpha = .8f; setPadding(0, 14, 0, 0) }
    private val panel = Panel().apply {
        orientation = LinearLayout.VERTICAL
        setPadding(22, 16, 22, 16)
        addView(header())
        addView(gauge, LinearLayout.LayoutParams(-1, 46).apply { topMargin = 10 })
        addView(row(R.string.screen_auto, auto(R.string.screen_hint_raise, "▲", ScreenCommand.RAISE),
            auto(R.string.screen_hint_stop, "■", ScreenCommand.STOP), auto(R.string.screen_hint_lower, "▼", ScreenCommand.LOWER)))
        addView(row(R.string.screen_by_hand, byHand(R.string.screen_hint_up, "▲", ScreenCommand.PRESS_UP),
            byHand(R.string.screen_hint_down, "▼", ScreenCommand.PRESS_DOWN)))
        addView(row(R.string.screen_is, auto(R.string.screen_hint_rolled, context.getString(R.string.screen_is_rolled), ScreenCommand.ROLLED_UP),
            auto(R.string.screen_hint_open, context.getString(R.string.screen_is_open), ScreenCommand.OPEN)))
        addView(hintLine)
    }

    init {
        isClickable = true
        addView(panel, LayoutParams(WIDTH, LayoutParams.WRAP_CONTENT))
    }

    /** Shows where the screen is now. */
    fun show(house: House) {
        val screen = house.screen ?: return
        val (words, numbers) = screenWords(screen)
        state.text = if (house.reachable) context.getString(words, *numbers.toTypedArray()) else context.getString(R.string.unreachable)
        gauge.show(screen.position, screen.target.takeIf { screen.moving != null })
    }

    /** Lays the panel over [tile], its bottom on the tile's, and gives the focus to ▼. */
    fun unfold(tile: View?) {
        val (x, bottom) = tile?.let { offsetIn(it) } ?: (Stage.WIDTH / 2 to Stage.HEIGHT / 2)
        panel.measure(MeasureSpec.makeMeasureSpec(WIDTH, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        (panel.layoutParams as LayoutParams).apply {
            leftMargin = x.coerceIn(MARGIN, Stage.WIDTH - WIDTH - MARGIN)
            topMargin = (bottom - panel.measuredHeight).coerceIn(MARGIN, Stage.HEIGHT - panel.measuredHeight - MARGIN)
        }
        panel.requestLayout()
        firstFocus?.requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { onClose(); return true }
        return super.dispatchKeyEvent(event)
    }

    /** The arrows stay on the panel's buttons. */
    override fun focusSearch(focused: View?, direction: Int): View? =
        super.focusSearch(focused, direction)?.takeIf { it.isDescendantOf(panel) } ?: focused

    /** Where [tile] starts and ends, in stage pixels: its left edge and its bottom. */
    private fun offsetIn(tile: View): Pair<Int, Int> {
        var x = 0f; var y = 0f; var view: View? = tile
        while (view != null && view !== parent) {
            x += view.left + view.translationX - ((view.parent as? View)?.scrollX ?: 0)
            y += view.top + view.translationY - ((view.parent as? View)?.scrollY ?: 0)
            view = view.parent as? View
        }
        return x.toInt() to (y + tile.height).toInt()
    }

    private fun header() = LinearLayout(context).apply {
        addView(text("▤  " + context.getString(R.string.screen).cased(), look.accent, 26f), LinearLayout.LayoutParams(0, -2, 1f))
        addView(state)
    }

    private fun row(what: Int, vararg buttons: TextView) = LinearLayout(context).apply {
        gravity = android.view.Gravity.CENTER_VERTICAL
        setPadding(0, 12, 0, 0)
        addView(text(context.getString(what), look.text, 20f).apply { alpha = .75f }, LinearLayout.LayoutParams(96, -2))
        buttons.forEachIndexed { i, b -> addView(b, LinearLayout.LayoutParams(0, 52, 1f).apply { if (i > 0) leftMargin = 10 }) }
    }

    /** A button that sends [command] on OK. */
    private fun auto(hintText: Int, label: String, command: ScreenCommand) = button(hintText, label).apply {
        setOnClickListener { onCommand(command) }
        if (command == ScreenCommand.LOWER) firstFocus = this
    }

    /** A button that sends [command] while OK is held, and stops the screen when it is let go. */
    private fun byHand(hintText: Int, label: String, command: ScreenCommand) = button(hintText, label).apply {
        setOnKeyListener { _, keyCode, event -> isOk(keyCode) && held(event, command) }
    }

    private fun held(event: KeyEvent, command: ScreenCommand): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) onCommand(command)
        if (event.action == KeyEvent.ACTION_UP) onCommand(ScreenCommand.STOP)
        return true
    }

    private fun button(hintText: Int, label: String): TextView = text(label, look.text, 24f).apply {
        gravity = android.view.Gravity.CENTER
        isFocusable = true
        isClickable = true
        background = Outline(look.text)
        setOnFocusChangeListener { v, has ->
            v.background = if (has) Filled(look.accent) else Outline(look.text)
            (v as TextView).setTextColor(if (has) look.panel or OPAQUE else look.text)
            if (has) hintLine.text = context.getText(hintText)
        }
    }

    private fun text(value: String, color: Int, size: Float): TextView = TextView(context).apply {
        text = value
        typeface = look.typeface
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        setTextColor(color)
        letterSpacing = if (look.upper) 0.08f else 0.02f
    }

    private fun String.cased() = if (look.upper) uppercase() else this

    private fun View.isDescendantOf(ancestor: View): Boolean {
        var view: View? = this
        while (view != null) { if (view === ancestor) return true; view = view.parent as? View }
        return false
    }

    /** The panel's paper and its accent edge. */
    private inner class Panel : LinearLayout(context) {
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.panel or OPAQUE }
        private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.accent; style = Paint.Style.STROKE; strokeWidth = 2f }

        init { setWillNotDraw(false) }

        override fun onDraw(canvas: Canvas) {
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fill)
            canvas.drawRect(1f, 1f, width - 1f, height - 1f, edge)
            canvas.drawRect(6f, 6f, width - 6f, height - 6f, edge)
        }
    }

    /** Rolled up on the left, open on the right: how far down the screen is, and where it is going. */
    private inner class Gauge : View(context) {
        private var position = 0
        private var target: Int? = null
        private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.text }
        private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.text; style = Paint.Style.STROKE; strokeWidth = 1.5f }
        private val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.accent; strokeWidth = 3f }
        private val words = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.text; alpha = 190; textSize = 16f; typeface = look.typeface }

        fun show(position: Int, target: Int?) {
            if (position == this.position && target == this.target) return
            this.position = position; this.target = target
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            canvas.drawRect(0f, 4f, w * position / 100f, 16f, ink)
            canvas.drawRect(0f, 4f, w, 16f, line)
            target?.let { canvas.drawLine(w * it / 100f, 0f, w * it / 100f, 20f, mark) }
            canvas.drawText(context.getString(R.string.screen_rolled), 0f, 40f, words)
            val open = context.getString(R.string.screen_opened)
            canvas.drawText(open, w - words.measureText(open), 40f, words)
        }
    }

    private class Outline(color: Int) : android.graphics.drawable.GradientDrawable() {
        init { setColor(Color.TRANSPARENT); setStroke(2, color) }
    }

    private class Filled(color: Int) : android.graphics.drawable.GradientDrawable() {
        init { setColor(color) }
    }

    private companion object {
        const val WIDTH = 410
        const val MARGIN = 20
        const val OPAQUE = 0xFF000000.toInt()

        fun isOk(keyCode: Int) = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
    }
}
