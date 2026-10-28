package com.l0rdd.pensieve.presentation.views.cyberpunk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrenchTime
import com.l0rdd.pensieve.presentation.views.common.HeroPanel
import java.time.LocalDateTime
import kotlin.random.Random

/** What to resume as a braindance in the editor: the poster split red and cyan, three tracks, the playhead. */
class BraindanceHero(context: Context, private val fonts: Fonts, resumeText: String, restartText: String, private val nothing: String) : HeroPanel {
    override val resume = CyberButton(context, fonts.rajdhaniBold, "▶ ${resumeText.uppercase()}")
    override val restart = CyberButton(context, fonts.rajdhaniBold, "↺ DÉBUT")
    private val panel = Panel(context)
    override val view = FrameLayout(context).apply {
        addView(panel, FrameLayout.LayoutParams(-1, -1))
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(resume, LinearLayout.LayoutParams(-2, 52).apply { rightMargin = 14 })
            addView(restart, LinearLayout.LayoutParams(-2, 52))
        }, FrameLayout.LayoutParams(-2, -2).apply { leftMargin = 246; topMargin = 228 })
    }

    override fun show(resume: Resume?, poster: Bitmap?, now: LocalDateTime?) { panel.show(resume, poster) }

    private inner class Panel(context: Context) : View(context) {
        private val tag = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 17f; letterSpacing = 0.18f }
        private val title = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 56f; color = CyberColors.TEXT }
        private val meta = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 18f; letterSpacing = 0.18f; color = CyberColors.MUTED }
        private val lane = Paint()
        private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
        private var resume: Resume? = null; private var poster: Bitmap? = null

        fun show(resume: Resume?, poster: Bitmap?) { this.resume = resume; this.poster = poster; invalidate() }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat(); val h = height.toFloat()
            CutFrame.draw(canvas, w, h, focused = false, cut = 22f)
            val r = resume
            val box = RectF(24f, 14f, 220f, h - 14f)
            poster?.let {
                bitmapPaint.alpha = 110; bitmapPaint.colorFilter = android.graphics.LightingColorFilter(0xFF4040, 0); canvas.drawBitmap(it, null, RectF(box.left - 4, box.top, box.right - 4, box.bottom), bitmapPaint)
                bitmapPaint.colorFilter = android.graphics.LightingColorFilter(0x40FFFF, 0); canvas.drawBitmap(it, null, RectF(box.left + 4, box.top, box.right + 4, box.bottom), bitmapPaint)
                bitmapPaint.alpha = 255; bitmapPaint.colorFilter = null; canvas.drawBitmap(it, null, box, bitmapPaint)
            } ?: run { lane.color = CyberColors.alpha(CyberColors.RED, 0.12f); canvas.drawRect(box, lane) }
            val x = 246f
            tag.color = 0xFF0B0B0B.toInt(); lane.color = CyberColors.YELLOW
            canvas.drawRect(x, 22f, x + tag.measureText("BRAINDANCE") + 22, 46f, lane)
            canvas.drawText("BRAINDANCE", x + 11, 41f, tag)
            tag.color = CyberColors.RED
            canvas.drawText(if (r == null) "// JELLYFIN // IDLE" else "// JELLYFIN // PAUSED", x + tag.measureText("BRAINDANCE") + 36, 41f, tag)
            canvas.drawText(TextUtils.ellipsize((r?.title ?: nothing).uppercase(), title, w - x - 30, TextUtils.TruncateAt.END).toString(), x, 108f, title)
            r ?: return
            canvas.drawText(listOfNotNull(r.year?.toString(), "${r.runtime.inWholeMinutes} MIN", r.genres.firstOrNull()?.uppercase()).joinToString(" / "), x, 140f, meta)
            // three tracks, seeded by the item so they stay put
            val random = Random(r.itemId.hashCode())
            val laneLeft = x + 120; val laneRight = w - 30
            listOf("VISUEL" to CyberColors.RED, "AUDIO" to CyberColors.CYAN, "THERMIQUE" to CyberColors.YELLOW).forEachIndexed { k, (name, color) ->
                val y = 166f + k * 24
                tag.color = color; tag.textSize = 14f; canvas.drawText(name, x, y + 13, tag); tag.textSize = 17f
                lane.color = 0x0AFFFFFF; canvas.drawRect(laneLeft, y, laneRight, y + 16, lane)
                var p = 0f
                while (p < 1f) {
                    val len = (2 + random.nextInt(9)) / 100f
                    if (random.nextInt(3) != 0) { lane.color = CyberColors.alpha(color, 0.35f + random.nextFloat() * 0.6f); canvas.drawRect(laneLeft + p * (laneRight - laneLeft), y + 2, laneLeft + minOf(1f, p + len) * (laneRight - laneLeft), y + 14, lane) }
                    p += len + 0.01f
                }
            }
            val head = laneLeft + r.progress.toFloat() * (laneRight - laneLeft)
            lane.color = CyberColors.YELLOW; canvas.drawRect(head - 1, 154f, head + 1, 240f, lane)
            tag.color = CyberColors.YELLOW; tag.textSize = 14f
            canvas.drawText(FrenchTime.clock(r.position.inWholeMinutes) + ":%02d".format(r.position.inWholeSeconds % 60), head - 28, 150f, tag); tag.textSize = 17f
        }
    }
}
