package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.ArtworkDrawing
import com.l0rdd.pensieve.presentation.views.common.Fonts

/** An app in the bottom strip: its artwork and its name, a dot of amber when it is new. */
class JarvisAppView(context: Context, fonts: Fonts) : View(context) {
    private val name = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 14f; letterSpacing = 0.15f; textAlign = Paint.Align.CENTER }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JarvisColors.GOLD }
    private var label = ""; private var art: Bitmap? = null; private var glyph: String? = null; private var isNew = false

    init { isFocusable = true; isClickable = true }

    fun show(label: String, art: Bitmap?, isNew: Boolean, glyph: String? = null) {
        this.label = label; this.art = art; this.isNew = isNew; this.glyph = glyph; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val color = if (isFocused) JarvisColors.GOLD else JarvisColors.CYAN
        Brackets.draw(canvas, w, h, color, size = 12f, all = false)
        val box = RectF(14f, 12f, w - 14f, h - 40f)
        art?.let { ArtworkDrawing.fit(canvas, it, box, 10f) }
        glyph?.let { canvas.drawText(it, w / 2, box.centerY() + 14f, name.apply { textSize = 38f; this.color = JarvisColors.GOLD }); name.textSize = 14f }
        name.color = if (isFocused) JarvisColors.TEXT else JarvisColors.MUTED
        canvas.drawText(TextUtils.ellipsize(label.uppercase(), name, w - 12f, TextUtils.TruncateAt.END).toString(), w / 2, h - 14f, name)
        if (isNew) canvas.drawCircle(w - 10f, 10f, 4.5f, dot)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        animate().scaleX(if (gainFocus) 1.06f else 1f).scaleY(if (gainFocus) 1.06f else 1f).translationY(if (gainFocus) -4f else 0f).setDuration(180).start()
    }
}
