package com.l0rdd.pensieve.presentation.views.panel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.l0rdd.pensieve.presentation.views.common.MenuStyle

/** One entry of the quick panel: its label, and a state on the right (ON, ▼ BAS, the look's name). */
data class PanelEntry(val label: String, val state: String, val onPick: () -> Unit)

/** The quick panel: a column on the right of the screen, dressed in the look's [MenuStyle]. */
class QuickPanelView(context: Context) : FrameLayout(context) {
    private val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(44, 56, 44, 56) }
    private var style: MenuStyle? = null
    private val rows = mutableListOf<Pair<TextView, TextView>>()
    private val panel = object : FrameLayout(context) {
        private val fill = Paint(); private val edge = Paint().apply { strokeWidth = 3f }
        init { setWillNotDraw(false); addView(column, LayoutParams(-1, -1)) }
        override fun onDraw(canvas: Canvas) {
            val s = style ?: return
            fill.color = s.panel; canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fill)
            edge.color = s.accent; canvas.drawLine(1.5f, 0f, 1.5f, height.toFloat(), edge)
        }
    }

    init { addView(panel, LayoutParams(560, -1, Gravity.END)) }

    /** Shows [title] and [entries]; rebuilt only when the entries' count changes, so the focus stays. */
    fun show(style: MenuStyle, title: String, entries: List<PanelEntry>) {
        val restyled = style != this.style
        this.style = style
        setBackgroundColor(style.scrim)
        if (restyled || rows.size != entries.size) build(style, title, entries.size)
        entries.forEachIndexed { i, e ->
            val (label, state) = rows[i]
            label.text = if (style.upper) e.label.uppercase() else e.label
            state.text = e.state
            (label.parent as View).setOnClickListener { e.onPick() }
        }
        panel.invalidate()
    }

    private fun build(style: MenuStyle, title: String, count: Int) {
        val focused = rows.indexOfFirst { (it.first.parent as View).isFocused }
        column.removeAllViews(); rows.clear()
        column.addView(text(style, if (style.upper) title.uppercase() else title, style.accent, style.textSize * 0.8f).apply { setPadding(20, 0, 20, 34) })
        repeat(count) {
            val label = text(style, "", style.text, style.textSize)
            val state = text(style, "", style.accent, style.textSize * 0.7f).apply { gravity = Gravity.END }
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 16, 20, 16); isFocusable = true; isClickable = true
                addView(label, LinearLayout.LayoutParams(0, -2, 1f)); addView(state)
                setOnFocusChangeListener { v, has ->
                    v.setBackgroundColor(if (has) style.accent else Color.TRANSPARENT)
                    val on = style.panel or 0xFF000000.toInt()
                    label.setTextColor(if (has) on else style.text); state.setTextColor(if (has) on else style.accent)
                }
            }
            column.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 6 })
            rows += label to state
        }
        (rows.getOrNull(focused.coerceAtLeast(0))?.first?.parent as? View)?.let { row -> row.post { row.requestFocus() } }
    }

    private fun text(style: MenuStyle, value: String, color: Int, size: Float) = TextView(context).apply {
        text = value; typeface = style.typeface; setTextSize(TypedValue.COMPLEX_UNIT_PX, size); setTextColor(color)
        letterSpacing = if (style.upper) 0.12f else 0.02f; includeFontPadding = false
    }
}
