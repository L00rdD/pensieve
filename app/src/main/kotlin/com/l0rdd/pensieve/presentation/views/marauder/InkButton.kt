package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/** A button drawn in ink, filled with red ink when it has the focus. */
class InkButton(context: Context, face: Typeface, text: String, private val inkColor: Int = InkColors.INK) : TextView(context) {
    private val frame = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        this.text = text; typeface = face
        setTextSize(TypedValue.COMPLEX_UNIT_PX, 21f); letterSpacing = 0.08f; gravity = Gravity.CENTER
        setPadding(24, 8, 24, 8)
        isFocusable = true; isClickable = true
        setTextColor(inkColor)
        setOnFocusChangeListener { _, has ->
            setTextColor(if (has) 0xFFF6E7C2.toInt() else inkColor)
            animate().rotation(if (has) -1f else 0f).scaleX(if (has) 1.05f else 1f).scaleY(if (has) 1.05f else 1f).setDuration(160).start()
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        if (isFocused) { frame.style = Paint.Style.FILL; frame.color = InkColors.RED; canvas.drawRect(0f, 0f, w, h, frame) }
        else { frame.style = Paint.Style.STROKE; frame.strokeWidth = 2f; frame.color = inkColor; canvas.drawRect(1f, 1f, w - 1, h - 1, frame) }
        super.onDraw(canvas)
    }
}
