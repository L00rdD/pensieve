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

    // the photo moves slowly: a few redraws a second are enough
    init { clock.add { t, _ -> if ((t * 8).toInt() != (page.t * 8).toInt()) { page.t = t; page.invalidate() } } }

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
        private var halftone: Bitmap? = null
        private var laidOut: String? = null
        private var headLayout: StaticLayout? = null; private var deckLayout: StaticLayout? = null; private var bodyLayout: StaticLayout? = null
        private var captionLayout: StaticLayout? = null

        fun show(resume: Resume?, poster: Bitmap?, now: LocalDateTime?) {
            val minute = now?.let { it.hour * 60 + it.minute }
            if (resume == this.resume && poster == this.poster && minute == this.now?.let { it.hour * 60 + it.minute }) return
            this.resume = resume; this.poster = poster; this.now = now; invalidate()
        }

        /** The halftone over the photo, drawn once. */
        private fun halftone(w: Int, h: Int): Bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
            val c = Canvas(bmp); var y = 0f
            while (y < h) { var x = 0f; while (x < w) { c.drawCircle(x, y, 1f, dots); x += 4f }; y += 4f }
        }

        /** Lays the text out again only when what it says changes. */
        private fun layout(width: Float) {
            val r = resume
            val key = listOf(r, now?.hour, now?.minute, width).toString()
            if (key == laidOut) return
            laidOut = key
            val x = 246f; val right = width - 24f
            val title = if (r == null) nothing else "${listOfNotNull(r.series, r.title).joinToString(" · ")} : la projection interrompue à ${FrenchTime.inWords((r.position.inWholeMinutes / 60).toInt(), (r.position.inWholeMinutes % 60).toInt())}"
            headLayout = StaticLayout.Builder.obtain(title, 0, title.length, head, (right - x).toInt()).setMaxLines(2).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
            captionLayout = StaticLayout.Builder.obtain(if (r != null) "Photographié sur le plateau." else "", 0, if (r != null) 28 else 0, small, 200).build()
            if (r == null) { deckLayout = null; bodyLayout = null; return }
            val end = now?.plusSeconds(r.remaining.inWholeSeconds)?.let { FrenchTime.inWords(it.hour, it.minute) } ?: "bientôt"
            val deckText = "Le film reprendra où vous l'avez laissé ; fin annoncée vers $end."
            deckLayout = StaticLayout.Builder.obtain(deckText, 0, deckText.length, deck, (right - x).toInt()).setMaxLines(1).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
            val bodyText = listOfNotNull(r.year?.toString(), "${r.runtime.inWholeMinutes} minutes", r.genres.firstOrNull()).joinToString(" · ") + ". " + r.overview
            bodyLayout = StaticLayout.Builder.obtain(bodyText, 0, bodyText.length, body, (((right - x) - 26) / 2).toInt()).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
        }

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
            val tone = halftone ?: halftone(box.width().toInt(), box.height().toInt()).also { halftone = it }
            canvas.drawBitmap(tone, box.left, box.top, null)
            canvas.restore()
            rule.style = Paint.Style.STROKE; rule.strokeWidth = 1f; canvas.drawRect(box, rule); rule.style = Paint.Style.FILL
            val r = resume
            layout(w)
            captionLayout?.let { canvas.save(); canvas.translate(18f, 258f); it.draw(canvas); canvas.restore() }
            // the masthead
            val x = 246f; val right = w - 24f
            canvas.drawRect(x, 18f, right, 22f, rule)
            canvas.drawText(special, x, 46f, small)
            canvas.drawText(masthead, (x + right) / 2, 48f, mast)
            canvas.drawText(now?.let { FrenchTime.longDate(it).substringBefore(' ') } ?: "", right - small.measureText("mercredi"), 46f, small)
            canvas.drawRect(x, 56f, right, 57f, rule); canvas.drawRect(x, 59f, right, 60f, rule)
            val headL = headLayout ?: return
            canvas.save(); canvas.translate(x, 70f); headL.draw(canvas); canvas.restore()
            r ?: return
            val top = 76f + headL.height
            deckLayout?.let { canvas.save(); canvas.translate(x, top); it.draw(canvas); canvas.restore() }
            bodyLayout?.let { bodyL ->
                val col = ((right - x) - 26) / 2; val lines = 3
                canvas.save(); canvas.translate(x, top + 32); canvas.clipRect(0f, 0f, col, lines * body.fontSpacing); bodyL.draw(canvas); canvas.restore()
                if (bodyL.lineCount > lines) {
                    canvas.save(); canvas.translate(x + col + 26, top + 32 - lines * body.fontSpacing); canvas.clipRect(0f, lines * body.fontSpacing, col, lines * 2 * body.fontSpacing); bodyL.draw(canvas); canvas.restore()
                    canvas.drawRect(x + col + 12, top + 34, x + col + 13, top + 30 + lines * body.fontSpacing, rule)
                }
            }
            // the red pin that holds the page on the map
            if (pin.shader == null) pin.shader = RadialGradient(w / 2 - 3, 2f, 10f, intArrayOf(0xFFE0605A.toInt(), 0xFF8C1D22.toInt(), 0xFF4A0C10.toInt()), floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
            canvas.drawCircle(w / 2, 4f, 8f, pin)
        }
    }
}
