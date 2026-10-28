package com.l0rdd.pensieve.presentation.views.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/** How a look dresses its menus. */
data class MenuStyle(
    val typeface: Typeface,
    val text: Int,
    val accent: Int,
    val panel: Int,
    val scrim: Int,
    val textSize: Float = 30f,
    val upper: Boolean = true,
)

/**
 * A menu over the home screen (arranging an app, picking the look): a column of entries in the
 * look's style. BACK closes it.
 */
class MenuOverlay(context: Context, private val look: MenuStyle, private val onClose: () -> Unit) : FrameLayout(context) {
    private val column = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 34, 40, 34) }
    private val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.panel }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = look.accent; style = Paint.Style.STROKE; strokeWidth = 2f }

    init {
        setBackgroundColor(look.scrim)
        isClickable = true
        addView(object : FrameLayout(context) {
            init { setWillNotDraw(false); addView(column) }
            override fun onDraw(canvas: Canvas) {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), panelPaint)
                canvas.drawRect(1f, 1f, width - 1f, height - 1f, edgePaint)
            }
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
    }

    /** Shows [title] then one entry per item; the first entry takes the focus. */
    fun show(title: String, items: List<Pair<String, () -> Unit>>) {
        column.removeAllViews()
        column.addView(label(title, look.accent, focusable = false))
        items.forEach { (text, action) -> column.addView(label(text, look.text, focusable = true).apply { setOnClickListener { action() } }) }
        column.getChildAt(1)?.requestFocus()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) { onClose(); return true }
        return super.dispatchKeyEvent(event)
    }

    private fun label(text: String, color: Int, focusable: Boolean) = TextView(context).apply {
        this.text = if (look.upper) text.uppercase() else text
        typeface = look.typeface
        setTextSize(TypedValue.COMPLEX_UNIT_PX, look.textSize)
        setTextColor(color)
        letterSpacing = if (look.upper) 0.12f else 0.02f
        setPadding(24, 14, 24, 14)
        isFocusable = focusable
        isClickable = focusable
        if (focusable) setOnFocusChangeListener { v, has ->
            v.setBackgroundColor(if (has) look.accent else Color.TRANSPARENT)
            setTextColor(if (has) look.panel or 0xFF000000.toInt() else color)
        }
    }

}
