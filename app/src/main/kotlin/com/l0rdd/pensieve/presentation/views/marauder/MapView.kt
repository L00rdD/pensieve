package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.random.Random

/**
 * The Marauder's Map: warm parchment (stains, folds, grain), corridors, rooms and stairs in ink
 * drawn once; people walking about with their names and footprints; and the user's own footsteps
 * walking through the corridors from one room's door to the next when the focus moves.
 */
class MapView(context: Context, private val italic: Typeface, clock: FrameClock) : View(context) {
    private var paper: Bitmap? = null
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG)
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = italic; textSize = 17f; textAlign = Paint.Align.CENTER }
    private val random = Random(5)
    private val people = listOf("Rusard", "Miss Teigne", "Peeves", "Albus Dumbledore").map { Walker(it, random.nextFloat() * 1920, random.nextFloat() * 1080) }
    private var walk: List<FloatArray>? = null
    private var walkT = 0f
    private var tick = 0f

    private inner class Walker(val name: String, var x: Float, var y: Float) {
        var tx = random.nextFloat() * 1920; var ty = random.nextFloat() * 1080; var d = 0f
        val trail = ArrayDeque<FloatArray>()
    }

    init {
        clock.add { _, dt ->
            people.forEach { p ->
                val dx = p.tx - p.x; val dy = p.ty - p.y; val dist = hypot(dx, dy)
                if (dist < 10) { p.tx = random.nextFloat() * 1920; p.ty = random.nextFloat() * 1080 }
                val v = 22 * dt; p.x += dx / dist * v; p.y += dy / dist * v; p.d += v
                if (p.d > 18) { p.d = 0f; p.trail.addLast(floatArrayOf(p.x, p.y, atan2(dy, dx), (p.trail.size % 2).toFloat())); if (p.trail.size > 10) p.trail.removeFirst() }
            }
            if (walk != null) { walkT += dt; if (walkT > 4.5f) walk = null }
            // the walkers are slow: 8 redraws a second; footsteps get every frame while they walk
            tick += dt
            if (walk != null || tick >= 0.125f) { tick = 0f; invalidate() }
        }
    }

    /** Footsteps from the door of [from] (bottom middle), along its corridor, down the aisle beside [to], to its door. */
    fun walk(from: Rect, to: Rect) {
        val g = 9f
        val ax = from.exactCenterX(); val ay = from.bottom + g; val bx = to.exactCenterX(); val by = to.bottom + g
        val vx = if (bx > ax || kotlin.math.abs(bx - ax) < 4) to.left - g else to.right + g
        walk = if (kotlin.math.abs(ay - by) < 4) listOf(floatArrayOf(ax, ay), floatArrayOf(bx, by))
        else listOf(floatArrayOf(ax, ay), floatArrayOf(vx, ay), floatArrayOf(vx, by), floatArrayOf(bx, by))
        walkT = 0f
    }

    override fun onDraw(canvas: Canvas) {
        val bmp = paper ?: draw(width, height).also { paper = it }
        canvas.drawBitmap(bmp, 0f, 0f, null)
        people.forEach { p ->
            p.trail.forEachIndexed { k, f -> step(canvas, f[0], f[1], f[2], f[3] > 0, 0.08f + 0.5f * k / p.trail.size, 1f) }
            val w = label.measureText(p.name) + 16
            ink.style = Paint.Style.FILL; ink.color = InkColors.alpha(0xFFF6E7C2.toInt(), 0.6f)
            canvas.drawRect(p.x - w / 2, p.y + 14, p.x + w / 2, p.y + 36, ink)
            ink.style = Paint.Style.STROKE; ink.strokeWidth = 1.2f; ink.color = InkColors.alpha(InkColors.INK, 0.5f)
            canvas.drawRect(p.x - w / 2, p.y + 14, p.x + w / 2, p.y + 36, ink)
            label.color = InkColors.alpha(InkColors.INK, 0.65f)
            canvas.drawText(p.name, p.x, p.y + 31, label)
        }
        walk?.let { pts ->
            val total = (1 until pts.size).sumOf { hypot(pts[it][0] - pts[it - 1][0], pts[it][1] - pts[it - 1][1]).toDouble() }.toFloat()
            val steps = maxOf(2f, total / 20f)
            var n = 0
            for (i in 1 until pts.size) {
                val (x0, y0) = pts[i - 1].let { it[0] to it[1] }; val (x1, y1) = pts[i].let { it[0] to it[1] }
                val len = hypot(x1 - x0, y1 - y0); val ang = atan2(y1 - y0, x1 - x0)
                var d = if (n > 0) 10f else 0f
                while (d < len) {
                    val age = walkT - n / steps * 1.1f
                    if (age < 0) return@let
                    val a = (0.9f - maxOf(0f, age - 1.6f) * 0.6f)
                    if (a > 0) step(canvas, x0 + (x1 - x0) * d / len, y0 + (y1 - y0) * d / len, ang, n % 2 == 1, a, 0.85f)
                    d += 20f; n++
                }
            }
        }
    }

    private fun step(canvas: Canvas, x: Float, y: Float, angle: Float, left: Boolean, alpha: Float, scale: Float) {
        ink.style = Paint.Style.FILL; ink.color = InkColors.alpha(InkColors.INK, alpha)
        canvas.save(); canvas.translate(x, y); canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat()); canvas.scale(scale, scale)
        canvas.translate(0f, if (left) -7f else 7f)
        canvas.drawOval(-7f, -3.6f, 7f, 3.6f, ink)
        canvas.drawOval(-11.5f, -2.8f, -5.5f, 2.8f, ink)
        canvas.restore()
    }

    /** The parchment and the castle in ink, drawn once at the view's size. */
    private fun draw(w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(w / 2f, h * 0.45f, w * 0.62f, intArrayOf(0xFFF6DDA6.toInt(), 0xFFE7BF7C.toInt(), 0xFFC58A43.toInt(), 0xFF9A5E24.toInt()), floatArrayOf(0f, 0.52f, 0.88f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        // stains
        listOf(floatArrayOf(0.18f, 0.72f, 170f, 0.22f), floatArrayOf(0.82f, 0.22f, 130f, 0.18f), floatArrayOf(0.63f, 0.88f, 210f, 0.2f), floatArrayOf(0.4f, 0.15f, 230f, 0.14f)).forEach { s ->
            p.shader = RadialGradient(s[0] * w, s[1] * h, s[2], InkColors.alpha(0xFF96501E.toInt(), s[3]), 0, Shader.TileMode.CLAMP)
            c.drawCircle(s[0] * w, s[1] * h, s[2], p)
        }
        // folds: two down, one across
        listOf(w / 3f, w * 2 / 3f).forEach { x ->
            p.shader = LinearGradient(x - 14, 0f, x + 14, 0f, intArrayOf(0, InkColors.alpha(0xFF5A3C14.toInt(), 0.2f), InkColors.alpha(0xFFFFFAE6.toInt(), 0.3f), 0), floatArrayOf(0f, 0.45f, 0.55f, 1f), Shader.TileMode.CLAMP)
            c.drawRect(x - 14, 0f, x + 14, h.toFloat(), p)
        }
        p.shader = LinearGradient(0f, h / 2f - 12, 0f, h / 2f + 12, intArrayOf(0, InkColors.alpha(0xFF5A3C14.toInt(), 0.16f), InkColors.alpha(0xFFFFFAE6.toInt(), 0.24f), 0), floatArrayOf(0f, 0.45f, 0.55f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, h / 2f - 12, w.toFloat(), h / 2f + 12, p)
        p.shader = null
        // grain
        val r = Random(9)
        p.style = Paint.Style.FILL
        repeat(26000) {
            p.color = InkColors.alpha(if (r.nextBoolean()) 0xFF6B3A10.toInt() else 0xFFFFF4D8.toInt(), r.nextFloat() * 0.09f)
            val x = r.nextFloat() * w; val y = r.nextFloat() * h
            c.drawRect(x, y, x + 1.6f, y + 1.6f, p)
        }
        // the castle
        p.style = Paint.Style.STROKE; p.color = InkColors.alpha(0xFF5A2D0A.toInt(), 0.24f); p.strokeWidth = 2f
        val q = Random(5)
        repeat(26) { val x = q.nextFloat() * 1800; val y = q.nextFloat() * 1000; val rw = 80 + q.nextFloat() * 220; val rh = 60 + q.nextFloat() * 160
            c.drawRect(x, y, x + rw, y + rh, p); if (q.nextFloat() < 0.4f) c.drawRect(x + 5, y + 5, x + rw - 5, y + rh - 5, p) }
        p.strokeWidth = 1.4f
        repeat(34) { val x = q.nextFloat() * w; val y = q.nextFloat() * h; val len = 200 + q.nextFloat() * 600
            if (q.nextBoolean()) { c.drawLine(x, y, x + len, y, p); c.drawLine(x, y + 26, x + len, y + 26, p) } else { c.drawLine(x, y, x, y + len, p); c.drawLine(x + 26, y, x + 26, y + len, p) } }
        repeat(8) { val x = q.nextFloat() * 1850; val y = q.nextFloat() * 1020; for (k in 0 until 9) c.drawLine(x + k * 7, y, x + k * 7, y + 40, p) }
        p.style = Paint.Style.FILL; p.typeface = italic; p.textSize = 20f; p.color = InkColors.alpha(0xFF3A2312.toInt(), 0.35f)
        listOf("Grande Salle" to (380f to 250f), "Tour d'astronomie" to (1500f to 140f), "Cachots" to (260f to 980f), "Bibliothèque" to (1640f to 900f), "Saule cogneur" to (1180f to 1040f))
            .forEach { (t, xy) -> c.drawText(t, xy.first, xy.second, p) }
        return bmp
    }
}
