package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/** A slanted button: red-framed, yellow with the focus. */
class CyberButton(context: Context, face: Typeface, text: String) : TextView(context) {
    private val shape = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        this.text = text; typeface = face
        setTextSize(TypedValue.COMPLEX_UNIT_PX, 20f); letterSpacing = 0.18f; gravity = Gravity.CENTER
        setPadding(28, 10, 34, 10)
        isFocusable = true; isClickable = true
        setTextColor(CyberColors.TEXT)
        setOnFocusChangeListener { _, has -> setTextColor(if (has) 0xFF0B0B0B.toInt() else CyberColors.TEXT); invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val path = Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(w - 12f, h); lineTo(0f, h); close() }
        shape.style = Paint.Style.FILL
        shape.color = if (isFocused) CyberColors.YELLOW else CyberColors.alpha(CyberColors.RED, 0.12f)
        canvas.drawPath(path, shape)
        if (!isFocused) { shape.style = Paint.Style.STROKE; shape.strokeWidth = 1.5f; shape.color = CyberColors.alpha(CyberColors.RED, 0.5f); canvas.drawPath(path, shape) }
        super.onDraw(canvas)
    }
}
