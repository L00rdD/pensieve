package com.l0rdd.pensieve.presentation.views.jarvis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextUtils
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.CYAN
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.GOLD
import com.l0rdd.pensieve.presentation.views.jarvis.JarvisColors.alpha
import kotlin.math.sin

/**
 * The arc reactor, Jellyfin's main action: the engraving turning round the casing, the ticks,
 * the arcs, the progress of what to resume in amber, the ten coils and the core with its title.
 * Cyan at rest, amber when it has the focus.
 */
class ReactorView(context: Context, fonts: Fonts, clock: FrameClock, private val engraving: String) : View(context) {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coil = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val engrave = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.michroma; textSize = 15f; letterSpacing = 0.25f }
    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.michroma; textSize = 21f; textAlign = Paint.Align.CENTER; color = 0xFFFFFFFF.toInt(); letterSpacing = 0.12f }
    private val action = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniBold; textSize = 16f; textAlign = Paint.Align.CENTER; letterSpacing = 0.25f }
    private val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = fonts.rajdhaniSemi; textSize = 15f; textAlign = Paint.Align.CENTER; letterSpacing = 0.15f; color = JarvisColors.MUTED }
    private val ring = Path()
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private var layers: Map<Int, Pair<android.graphics.Bitmap, android.graphics.Bitmap>> = emptyMap()
    private var coreShader: android.graphics.Shader? = null
    private var t = 0f
    private var name = ""; private var verb = ""; private var detail = ""; private var progress = 0f

    init {
        isFocusable = true; isClickable = true
        clock.add { time, _ -> t = time; invalidate() }
    }

    fun show(name: String, verb: String, detail: String, progress: Float) {
        this.name = name; this.verb = verb; this.detail = detail; this.progress = progress; invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val c = width / 2f; val cy = height / 2f
        val color = if (isFocused) GOLD else CYAN
        val r = width / 2f
        val (engraved, ticks) = layers[color] ?: layersFor(color, r).also { layers = layers + (color to it) }
        // the engraving, turning
        canvas.save(); canvas.rotate(t * 6f, c, cy); canvas.drawBitmap(engraved, c - engraved.width / 2f, cy - engraved.height / 2f, bitmapPaint); canvas.restore()
        // ticks
        canvas.save(); canvas.rotate(t * 4f, c, cy); canvas.drawBitmap(ticks, c - ticks.width / 2f, cy - ticks.height / 2f, bitmapPaint); canvas.restore()
        // progress, amber
        val box = RectF(c - r * 0.93f, cy - r * 0.93f, c + r * 0.93f, cy + r * 0.93f)
        stroke.strokeWidth = 5f; stroke.color = alpha(GOLD, 0.15f); canvas.drawArc(box, 0f, 360f, false, stroke)
        stroke.color = GOLD; canvas.drawArc(box, -90f, 360f * progress, false, stroke)
        // three arcs, turning back
        canvas.save(); canvas.rotate(-t * (if (isFocused) 120f else 26f), c, cy)
        stroke.color = color; stroke.strokeWidth = 3f
        val arcs = RectF(c - r * 0.86f, cy - r * 0.86f, c + r * 0.86f, cy + r * 0.86f)
        canvas.drawArc(arcs, 0f, 70f, false, stroke); canvas.drawArc(arcs, 120f, 80f, false, stroke); canvas.drawArc(arcs, 250f, 70f, false, stroke)
        canvas.restore()
        // the ten coils, pulsing
        // a thick stroke on the ring's middle gives the coils without any clipping
        val outer = r * 0.85f; val inner = r * 0.62f; val mid = (outer + inner) / 2
        coil.color = alpha(color, 0.75f + 0.25f * sin(t * 2f)); coil.strokeWidth = outer - inner
        val coils = RectF(c - mid, cy - mid, c + mid, cy + mid)
        for (i in 0 until 10) canvas.drawArc(coils, i * 36f + 3f - 90f, 30f, false, coil)
        // the core
        val core = r * 0.5f
        fill.shader = coreShader ?: RadialGradient(c, cy, core, intArrayOf(0xFF1D6A86.toInt(), 0xFF0B2B3A.toInt(), 0xFF04121A.toInt()), floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP).also { coreShader = it }
        canvas.drawCircle(c, cy, core, fill); fill.shader = null
        stroke.color = color; stroke.strokeWidth = 2f; canvas.drawCircle(c, cy, core, stroke)
        stroke.color = alpha(color, 0.22f); stroke.strokeWidth = 3f
        canvas.drawPath(Path().apply { moveTo(c, cy - core * 0.62f); lineTo(c + core * 0.6f, cy + core * 0.42f); lineTo(c - core * 0.6f, cy + core * 0.42f); close() }, stroke)
        val shown = TextUtils.ellipsize(name, android.text.TextPaint(title), core * 1.7f, TextUtils.TruncateAt.END).toString()
        canvas.drawText(shown, c, cy - 6f, title)
        action.color = color
        canvas.drawText(verb, c, cy + 22f, action)
        canvas.drawText(detail, c, cy + 46f, sub)
    }

    /** The engraving and the ticks of one colour, drawn once. */
    private fun layersFor(color: Int, r: Float): Pair<android.graphics.Bitmap, android.graphics.Bitmap> {
        val side = (r * 2.5f).toInt()
        val half = side / 2f
        val engraved = android.graphics.Bitmap.createBitmap(side, side, android.graphics.Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(engraved).apply {
            ring.reset(); ring.addCircle(half, half, r * 1.08f, Path.Direction.CW)
            engrave.color = alpha(color, 0.75f)
            val around = (2 * Math.PI * r * 1.08f).toFloat()
            val once = engrave.measureText(engraving).coerceAtLeast(1f)
            drawTextOnPath(engraving.repeat((around / once).toInt().coerceAtLeast(1)), ring, 0f, 0f, engrave)
        }
        val ticks = android.graphics.Bitmap.createBitmap(side, side, android.graphics.Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(ticks).apply {
            stroke.color = alpha(color, 0.55f); stroke.strokeWidth = 1.6f
            for (i in 0 until 72) { drawLine(half, half - r, half, half - r + 9, stroke); rotate(5f, half, half) }
        }
        return engraved to ticks
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        animate().scaleX(if (gainFocus) 1.04f else 1f).scaleY(if (gainFocus) 1.04f else 1f).setDuration(220).start()
    }
}
