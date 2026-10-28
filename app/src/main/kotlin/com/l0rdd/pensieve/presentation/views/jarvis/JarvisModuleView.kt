package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.ArtworkDrawing
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.CYAN
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.GOLD

/**
 * A module round the reactor (a favourite): "01 // NETFLIX", its artwork, and a callout line to
 * the reactor on the [left] or right side. With the focus a reticle closes in on the artwork.
 */
class JarvisModuleView(context: Context, fonts: Fonts, clock: FrameClock, private val left: Boolean) : View(context) {
    private val id = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 21f; letterSpacing = 0.2f }
    private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 17f; letterSpacing = 0.18f; color = JarvisColors.MUTED }
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 1f }
    private val reticle = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1.5f; color = GOLD; pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) }
    private val lock = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 14f; letterSpacing = 0.3f; color = GOLD }
    private var number = ""; private var name = ""; private var detail = ""; private var art: Bitmap? = null; private var glyph: String? = null
    private var t = 0f; private var lockedAt = 0f

    init {
        isFocusable = true; isClickable = true
        clock.add { time, _ -> t = time; if (isFocused) invalidate() }
    }

    fun show(number: String, name: String, detail: String, art: Bitmap?, glyph: String? = null) {
        this.number = number; this.name = name; this.detail = detail; this.art = art; this.glyph = glyph; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val color = if (isFocused) GOLD else CYAN
        Brackets.draw(canvas, w, h, color)
        canvas.drawText(number, 22f, 42f, id.apply { this.color = JarvisColors.TEXT })
        val x = 22f + id.measureText("$number ")
        canvas.drawText("//", x, 42f, id.apply { this.color = color })
        canvas.drawText(" ${name.uppercase()}", x + id.measureText("//"), 42f, id.apply { this.color = JarvisColors.TEXT })
        canvas.drawText(android.text.TextUtils.ellipsize(detail.uppercase(), android.text.TextPaint(sub), w - 190f, android.text.TextUtils.TruncateAt.END).toString(), 22f, 76f, sub)
        val box = RectF(w - 150f, h / 2 - 36f, w - 20f, h / 2 + 36f)
        art?.let { ArtworkDrawing.fit(canvas, it, box) }
        glyph?.let { canvas.drawText(it, box.centerX(), box.centerY() + 16f, sub.apply { textSize = 44f; textAlign = Paint.Align.CENTER; this.color = color }); sub.textSize = 17f; sub.textAlign = Paint.Align.LEFT; sub.color = JarvisColors.MUTED }
        if (isFocused) {
            val k = ((t - lockedAt) / 0.35f).coerceIn(0f, 1f)
            val radius = 48f * (2.4f - 1.4f * k)
            reticle.alpha = (255 * k).toInt()
            canvas.save(); canvas.rotate(t * 60f, box.centerX(), box.centerY())
            canvas.drawCircle(box.centerX(), box.centerY(), radius, reticle)
            canvas.restore()
            canvas.drawText("◎ LOCK", 22f, h - 16f, lock)
        }
        // the callout line, drawn outside the module towards the reactor
        line.color = JarvisColors.alpha(CYAN, 0.5f)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        lockedAt = t
        animate().scaleX(if (gainFocus) 1.04f else 1f).scaleY(if (gainFocus) 1.04f else 1f).setDuration(200).start()
    }
}
