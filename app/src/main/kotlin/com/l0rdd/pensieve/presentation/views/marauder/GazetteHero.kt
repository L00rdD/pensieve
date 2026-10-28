package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.common.FrenchTime
import com.l0rdd.pensieve.presentation.views.common.HeroPanel
import java.time.LocalDateTime
import kotlin.math.sin

/** What to resume as the Gazette's front page pinned to the map, its photo moving as the Gazette's do. */
class GazetteHero(context: Context, private val fonts: Fonts, clock: FrameClock, private val masthead: String, private val special: String, private val nothing: String) : HeroPanel {
    override val resume = InkButton(context, fonts.fell, "Reprendre la lecture", InkColors.NEWS_INK)
    override val restart = InkButton(context, fonts.fell, "Depuis le début", InkColors.NEWS_INK)
    private val page = Page(context)
    override val view = FrameLayout(context).apply {
        rotation = -0.35f
        addView(page, FrameLayout.LayoutParams(-1, -1))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(resume, LinearLayout.LayoutParams(-2, 50).apply { rightMargin = 14 })
            addView(restart, LinearLayout.LayoutParams(-2, 50))
        }, FrameLayout.LayoutParams(-2, -2).apply { leftMargin = 246; topMargin = 248 })
    }

    init { clock.add { t, _ -> page.t = t; page.invalidate() } }

    override fun show(resume: Resume?, poster: Bitmap?, now: LocalDateTime?) { page.show(resume, poster, now) }

    private inner class Page(context: Context) : View(context) {
        var t = 0f
        private val paper = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = InkColors.NEWSPRINT }
        private val rule = Paint().apply { color = InkColors.NEWS_INK }
        private val small = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 15f; color = InkColors.NEWS_INK }
        private val mast = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 27f; color = InkColors.NEWS_INK; textAlign = Paint.Align.CENTER; letterSpacing = 0.06f }
        private val head = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 42f; color = InkColors.NEWS_INK }
        private val deck = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fellItalic; textSize = 22f; color = 0xFF3A2E20.toInt() }
        private val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.fell; textSize = 16f; color = 0xFF2A2218.toInt() }
        private val photo = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f); postConcat(ColorMatrix(floatArrayOf(1.1f, 0f, 0f, 0f, 18f, 0f, 1.0f, 0f, 0f, 6f, 0f, 0f, 0.85f, 0f, -8f, 0f, 0f, 0f, 1f, 0f))) })
        }
        private val dots = Paint().apply { color = InkColors.alpha(0xFF1E140A.toInt(), 0.22f) }
        private val pin = Paint(Paint.ANTI_ALIAS_FLAG)
        private var resume: Resume? = null; private var poster: Bitmap? = null; private var now: LocalDateTime? = null

        fun show(resume: Resume?, poster: Bitmap?, now: LocalDateTime?) { this.resume = resume; this.poster = poster; this.now = now; invalidate() }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat(); val h = height.toFloat()
            canvas.drawRect(0f, 0f, w, h, paper)
            // the photo, moving a little
            val box = RectF(18f, 16f, 218f, 252f)
            canvas.save(); canvas.clipRect(box)
            poster?.let {
                val k = 1f + 0.02f * (1 + sin(t / 4.5f)); val dx = -3f * sin(t / 4.5f)
                canvas.drawBitmap(it, null, RectF(box.left + dx, box.top, box.left + dx + box.width() * k, box.top + box.height() * k), photo)
            } ?: canvas.drawColor(0xFFD8C9A6.toInt())
            var y = box.top; while (y < box.bottom) { var x = box.left; while (x < box.right) { canvas.drawCircle(x, y, 1f, dots); x += 4f }; y += 4f }
            canvas.restore()
            rule.style = Paint.Style.STROKE; rule.strokeWidth = 1f; canvas.drawRect(box, rule); rule.style = Paint.Style.FILL
            val r = resume
            StaticLayout.Builder.obtain(if (r != null) "Photographié sur le plateau." else "", 0, if (r != null) 28 else 0, small, 200).build().let { canvas.save(); canvas.translate(18f, 258f); it.draw(canvas); canvas.restore() }
            // the masthead
            val x = 246f; val right = w - 24f
            canvas.drawRect(x, 18f, right, 22f, rule)
            canvas.drawText(special, x, 46f, small)
            canvas.drawText(masthead, (x + right) / 2, 48f, mast)
            canvas.drawText(now?.let { FrenchTime.longDate(it).substringBefore(' ') } ?: "", right - small.measureText("mercredi"), 46f, small)
            canvas.drawRect(x, 56f, right, 57f, rule); canvas.drawRect(x, 59f, right, 60f, rule)
            val title = if (r == null) nothing else "${r.title} : la projection interrompue à ${FrenchTime.inWords((r.position.inWholeMinutes / 60).toInt(), (r.position.inWholeMinutes % 60).toInt())}"
            val headLayout = StaticLayout.Builder.obtain(title, 0, title.length, head, (right - x).toInt()).setMaxLines(2).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
            canvas.save(); canvas.translate(x, 70f); headLayout.draw(canvas); canvas.restore()
            r ?: return
            val end = now?.plusSeconds(r.remaining.inWholeSeconds)?.let { FrenchTime.inWords(it.hour, it.minute) } ?: "bientôt"
            val deckText = "Le film reprendra où vous l'avez laissé ; fin annoncée vers $end."
            val top = 76f + headLayout.height
            StaticLayout.Builder.obtain(deckText, 0, deckText.length, deck, (right - x).toInt()).setMaxLines(1).setEllipsize(android.text.TextUtils.TruncateAt.END).build().let { canvas.save(); canvas.translate(x, top); it.draw(canvas); canvas.restore() }
            val bodyText = listOfNotNull(r.year?.toString(), "${r.runtime.inWholeMinutes} minutes", r.genres.firstOrNull()).joinToString(" · ") + ". " + r.overview
            val col = ((right - x) - 26) / 2
            val bodyLayout = StaticLayout.Builder.obtain(bodyText, 0, bodyText.length, body, col.toInt()).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
            val lines = 3
            canvas.save(); canvas.translate(x, top + 32); canvas.clipRect(0f, 0f, col, lines * body.fontSpacing); bodyLayout.draw(canvas); canvas.restore()
            if (bodyLayout.lineCount > lines) {
                canvas.save(); canvas.translate(x + col + 26, top + 32 - lines * body.fontSpacing); canvas.clipRect(0f, lines * body.fontSpacing, col, lines * 2 * body.fontSpacing); bodyLayout.draw(canvas); canvas.restore()
                canvas.drawRect(x + col + 12, top + 34, x + col + 13, top + 30 + lines * body.fontSpacing, rule)
            }
            // the red pin that holds the page on the map
            pin.shader = RadialGradient(w / 2 - 3, 2f, 10f, intArrayOf(0xFFE0605A.toInt(), 0xFF8C1D22.toInt(), 0xFF4A0C10.toInt()), floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
            canvas.drawCircle(w / 2, 4f, 8f, pin)
        }
    }
}
