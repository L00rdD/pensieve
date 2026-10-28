package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spanned
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.math.abs
import kotlin.math.sin

/** J.A.R.V.I.S. speaking at the bottom: its name, the voice bars, and one line typed out. */
class JarvisVoiceView(context: Context, fonts: Fonts, clock: FrameClock) : View(context) {
    private val who = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.michroma; textSize = 16f; letterSpacing = 0.25f; color = JarvisColors.CYAN }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 23f; letterSpacing = 0.06f }
    private val bar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JarvisColors.CYAN }
    private val rule = Paint().apply { color = JarvisColors.alpha(JarvisColors.CYAN, 0.25f) }
    private var line = ""; private var gold = emptyList<IntRange>(); private var typed = 0; private var t = 0f

    init {
        clock.add { time, dt ->
            val blink = (time * 2).toInt() != (t * 2).toInt()
            t = time
            val typing = typed < line.length
            if (typing) typed = minOf(line.length, typed + (dt * 70).toInt().coerceAtLeast(1))
            if (typing || blink) invalidate()
        }
    }

    /** Says [html]; the parts in bold come out in amber. */
    fun say(html: Spanned) {
        if (html.toString() == line) return
        line = html.toString(); typed = 0
        gold = html.getSpans(0, html.length, android.text.style.StyleSpan::class.java).map { html.getSpanStart(it) until html.getSpanEnd(it) }
    }

    override fun onDraw(canvas: Canvas) {
        val y = height / 2f + 8f
        canvas.drawText("J.A.R.V.I.S.", 0f, y, who)
        val talking = typed < line.length
        for (i in 0 until 5) {
            val k = if (talking) 0.2f + 0.8f * abs(sin(t * 9f + i * 1.3f)) else 0.2f
            canvas.drawRect(170f + i * 6f, height / 2f - 12f * k, 173f + i * 6f, height / 2f + 12f * k, bar)
        }
        canvas.drawRect(222f, 4f, 223f, height - 4f, rule)
        var x = 240f
        for (i in 0 until typed) {
            text.color = if (gold.any { i in it }) JarvisColors.GOLD else JarvisColors.TEXT
            val ch = line[i].toString()
            canvas.drawText(ch, x, y, text); x += text.measureText(ch)
        }
        if ((t * 2).toInt() % 2 == 0) { text.color = JarvisColors.CYAN; canvas.drawText("▍", x + 2, y, text) }
    }
}
