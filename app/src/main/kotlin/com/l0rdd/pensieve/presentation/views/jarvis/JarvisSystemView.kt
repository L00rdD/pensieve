package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.Fonts

/** A system of the house below the apps: the cinema mode, an outlet, the screen (with its gauge). */
class JarvisSystemView(context: Context, fonts: Fonts) : View(context) {
    private val id = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 20f; letterSpacing = 0.2f }
    private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 16f; letterSpacing = 0.18f; color = JarvisColors.MUTED }
    private val state = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 17f; letterSpacing = 0.25f; color = JarvisColors.GOLD }
    private val big = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 46f; textAlign = Paint.Align.CENTER }
    private val bar = Paint()
    private var kind = ""; private var name = ""; private var detail = ""; private var status = ""; private var glyph = ""; private var gauge: Float? = null

    init { isFocusable = true; isClickable = true }

    fun show(kind: String, name: String, detail: String, status: String, glyph: String, gauge: Float? = null) {
        this.kind = kind; this.name = name; this.detail = detail; this.status = status; this.glyph = glyph; this.gauge = gauge; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val color = if (isFocused) JarvisColors.GOLD else JarvisColors.CYAN
        Brackets.draw(canvas, w, h, color)
        id.color = JarvisColors.TEXT
        canvas.drawText(kind, 22f, 40f, id)
        val x = 22f + id.measureText("$kind ")
        canvas.drawText("//", x, 40f, id.apply { this.color = color })
        canvas.drawText(" ${name.uppercase()}", x + id.measureText("//"), 40f, id.apply { this.color = JarvisColors.TEXT })
        gauge?.let {
            bar.color = JarvisColors.alpha(JarvisColors.CYAN, 0.15f); canvas.drawRect(22f, 60f, w - 22f, 66f, bar)
            bar.color = color; canvas.drawRect(22f, 60f, 22f + (w - 44f) * it, 66f, bar)
        }
        canvas.drawText(detail.uppercase(), 22f, if (gauge != null) 96f else 74f, sub)
        canvas.drawText(status, 22f, h - 18f, state)
        big.color = color
        if (gauge == null) canvas.drawText(glyph, w - 50f, 92f, big)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        animate().scaleX(if (gainFocus) 1.03f else 1f).scaleY(if (gainFocus) 1.03f else 1f).setDuration(200).start()
    }
}
