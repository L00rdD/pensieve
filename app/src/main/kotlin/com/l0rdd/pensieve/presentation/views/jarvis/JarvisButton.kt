package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/** A slanted HUD button ("↺ DEPUIS LE DÉBUT", "⚙ PARAMÈTRES"): outlined in cyan, filled amber with the focus. */
class JarvisButton(context: Context, typeface: Typeface, text: String, size: Float = 17f) : TextView(context) {
    private val shape = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        this.text = text
        this.typeface = typeface
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
        letterSpacing = 0.25f
        gravity = Gravity.CENTER
        setPadding(30, 10, 30, 10)
        isFocusable = true; isClickable = true
        setTextColor(JarvisColors.CYAN)
        setOnFocusChangeListener { _, has -> setTextColor(if (has) 0xFF1A1206.toInt() else JarvisColors.CYAN); invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val path = Path().apply { moveTo(10f, 0f); lineTo(w, 0f); lineTo(w - 10f, h); lineTo(0f, h); close() }
        if (isFocused) { shape.style = Paint.Style.FILL; shape.color = JarvisColors.GOLD }
        else { shape.style = Paint.Style.STROKE; shape.strokeWidth = 1.5f; shape.color = JarvisColors.alpha(JarvisColors.CYAN, 0.45f) }
        canvas.drawPath(path, shape)
        super.onDraw(canvas)
    }
}
