package com.l0rdd.pensieve.presentation.views.marauder

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.random.Random

/**
 * The spell's ink over a panel [w] × [h]: the drop at the top centre, the tendrils growing from it,
 * and the blooming blobs the panel shows through, soft at their edge, as [timeline] says at each moment.
 */
class SpellInk(w: Float, h: Float, val timeline: SpellTimeline, random: Random = Random(System.nanoTime())) {
    private val ox = w / 2
    private val oy = 0f
    private val blobs = blooms(ox, oy)
    private val lines = tendrils(ox, oy, w, h, random)
    private val soft = RadialGradient(0f, 0f, 1f, intArrayOf(Color.BLACK, Color.BLACK, Color.TRANSPARENT), floatArrayOf(0f, .78f, 1f), Shader.TileMode.CLAMP)
    private val blob = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = soft }
    private val masking = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
    private val place = Matrix()
    private val pen = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; color = InkColors.INK }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = InkColors.INK }

    /** Draws [content] seen only through the blobs as they are at [ms]. */
    fun masked(canvas: Canvas, ms: Long, content: () -> Unit) {
        val layer = canvas.saveLayer(null, null)
        content()
        canvas.saveLayer(null, masking)
        blobs.forEach { drawBlob(canvas, it, timeline.radius(it, ms)) }
        canvas.restoreToCount(layer)
    }

    /** Draws the drop and the tendrils as they are at [ms]. */
    fun ink(canvas: Canvas, ms: Long) {
        val grow = timeline.grow(ms); val fade = timeline.fade(ms)
        dot.alpha = alpha(.85f * fade)
        canvas.drawCircle(ox, oy, timeline.drop(ms), dot)
        lines.forEach { drawTendril(canvas, it, grow, fade) }
    }

    private fun drawBlob(canvas: Canvas, b: Bloom, radius: Float) {
        if (radius <= 0f) return
        place.setScale(radius, radius); place.postTranslate(b.x, b.y)
        soft.setLocalMatrix(place)
        canvas.drawCircle(b.x, b.y, radius, blob)
    }

    /** A tendril, as far as it has grown: thick and dark at the drop, thinner and paler on its way, sometimes ending on a blot. */
    private fun drawTendril(canvas: Canvas, line: Tendril, grow: Float, fade: Float) {
        val n = line.shown(grow)
        for (k in 1 until n) {
            val along = k.toFloat() / line.size
            pen.alpha = alpha(.75f * fade * (1 - along * .6f)); pen.strokeWidth = 2.6f * (1 - along) + .5f
            canvas.drawLine(line.xs[k - 1], line.ys[k - 1], line.xs[k], line.ys[k], pen)
        }
        if (line.blot && n == line.size) canvas.drawCircle(line.xs[n - 1], line.ys[n - 1], 2.6f, dot)
    }

    private fun alpha(a: Float) = (clamp01(a) * 255).toInt()
}
