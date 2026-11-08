package com.l0rdd.pensieve.presentation.views.marauder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.presentation.views.common.FrameClock
import com.l0rdd.pensieve.presentation.views.common.HouseThing
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Weasleys' clock, [WIDTH] × [HEIGHT]: Hermione's Time-Turner turning behind it, without its
 * chain, its ring round a dial whose face lets the hourglass show through; the ten cases round
 * the rim, the time's thin black hands, and one gold hand per thing of the house, a medallion with
 * its engraving at the end, springing to its case. The [picked] thing's hand is drawn last, ringed in red.
 */
class WeasleyClockView(context: Context, private val face: Typeface, inscription: String, clock: FrameClock) : View(context) {
    private val turner = TimeTurnerDrawing(face, inscription)
    private val icons = WeasleyIcons()
    private val dial = WeasleyDial(context, face)
    private val gold = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val cream = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CREAM }
    private val shaft = Path()
    private val shafts = HashMap<Float, Shader>()
    private val medalGold = LinearGradient(-MEDAL, -MEDAL, MEDAL, MEDAL, GOLD, null, Shader.TileMode.CLAMP)
    private var faceBitmap: Bitmap? = null
    private var things = emptyList<HouseThing>()
    private var house = House.UNKNOWN
    private var targets = emptyList<Aim>()
    private var springs = emptyList<Spring>()
    private var angles = FloatArray(0)
    private var now: LocalDateTime? = null
    private var t = 0f

    /** The index of the thing whose row has the focus, -1 for none. */
    var picked = -1
        set(value) { field = value; invalidate() }

    init { clock.add { time, dt -> t = time; swing(dt); invalidate() } }

    fun showTime(now: LocalDateTime) { this.now = now; turner.show(now); invalidate() }

    fun show(things: List<HouseThing>, house: House) {
        this.things = things; this.house = house
        targets = aims(things, house)
        if (springs.size != things.size) springs = List(things.size) { springs.getOrNull(it) ?: Spring() }
        angles = FloatArray(things.size) { springs[it].angle }
        invalidate()
    }

    /** Each hand on by [dt] seconds, trembling when its thing is shaky. */
    private fun swing(dt: Float) {
        for (i in springs.indices) {
            val angle = springs[i].step(targets[i].angle, dt)
            angles[i] = angle + if (isShaky(things[i], house)) tremble(t + i, angle) else 0f
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save(); canvas.translate(CX - TimeTurnerDrawing.CX * TURNER, CY - TimeTurnerDrawing.CY * TURNER); canvas.scale(TURNER, TURNER)
        turner.draw(canvas, t, chain = false)
        canvas.restore()
        canvas.drawBitmap(faceBitmap ?: dial.draw(WIDTH, HEIGHT, CX, CY, R).also { faceBitmap = it }, 0f, 0f, null)
        now?.let { timeHands(canvas, it) }
        val order = things.indices.filter { it != picked } + listOfNotNull(picked.takeIf { it in things.indices })
        order.forEach { houseHand(canvas, it) }
        boss(canvas)
    }

    /** The hour and minute hands, thin and black, under the house's. */
    private fun timeHands(canvas: Canvas, now: LocalDateTime) {
        timeHand(canvas, (now.hour % 12 + now.minute / 60f) * 30f, R * 0.38f, 4f)
        timeHand(canvas, now.minute * 6f, R * 0.55f, 2.5f)
    }

    private fun timeHand(canvas: Canvas, angle: Float, length: Float, width: Float) {
        canvas.save(); canvas.translate(CX, CY); canvas.rotate(angle)
        ink.color = InkColors.NEWS_INK; ink.strokeWidth = width; ink.style = Paint.Style.STROKE
        canvas.drawLine(0f, 12f, 0f, -length, ink)
        shaft.reset(); shaft.moveTo(0f, -length); shaft.lineTo(-5f, -length + 9); shaft.lineTo(5f, -length + 9); shaft.close()
        ink.style = Paint.Style.FILL; canvas.drawPath(shaft, ink)
        canvas.restore()
    }

    /** The [i]th thing's gold hand, long or short, and its medallion kept upright. */
    private fun houseHand(canvas: Canvas, i: Int) {
        val angle = angles.getOrElse(i) { 0f }
        val length = if (targets[i].short) R - 98 else R - 62
        canvas.save(); canvas.translate(CX, CY); canvas.rotate(angle)
        drawShaft(canvas, length)
        canvas.translate(0f, -length); canvas.rotate(-angle)
        medallion(canvas, iconOf(things[i], house), i == picked)
        canvas.restore()
    }

    /** The hand's tapering shaft and the little scroll on it. */
    private fun drawShaft(canvas: Canvas, length: Float) {
        gold.shader = shafts.getOrPut(length) { LinearGradient(-6f, 0f, 6f, -length, GOLD, null, Shader.TileMode.CLAMP) }
        edge.color = InkColors.alpha(0xFF5A3A0E.toInt(), 0.8f); edge.strokeWidth = 0.8f; edge.shader = null
        shaft.reset(); shaft.moveTo(-5f, 10f); shaft.quadTo(-1.5f, -length * 0.5f, -2f, -length + MEDAL); shaft.lineTo(2f, -length + MEDAL)
        shaft.quadTo(1.5f, -length * 0.5f, 5f, 10f); shaft.close()
        canvas.drawPath(shaft, gold); canvas.drawPath(shaft, edge)
        val y = -length * 0.42f
        canvas.drawOval(-4.5f, y - 7, 4.5f, y + 7, gold); canvas.drawOval(-4.5f, y - 7, 4.5f, y + 7, edge)
        gold.shader = null
    }

    /** The cream medallion with its engraving, ringed in gold, or in red with a glow when [selected]. */
    private fun medallion(canvas: Canvas, icon: Icon, selected: Boolean) {
        if (selected) cream.setShadowLayer(14f, 0f, 0f, InkColors.alpha(InkColors.RED, 0.7f)) else cream.clearShadowLayer()
        canvas.drawCircle(0f, 0f, MEDAL, cream)
        ring(selected)
        canvas.drawCircle(0f, 0f, MEDAL, edge)
        edge.shader = null
        canvas.translate(0f, 1f)
        icons.draw(canvas, icon, MEDAL / 15f, if (selected) InkColors.RED else InkColors.NEWS_INK)
    }

    /** The medallion's ring: red and a little thicker when [selected], gold otherwise. */
    private fun ring(selected: Boolean) {
        edge.color = InkColors.RED
        edge.strokeWidth = if (selected) 3.5f else 3f
        edge.shader = if (selected) null else medalGold
    }

    /** The gold boss holding the hands. */
    private fun boss(canvas: Canvas) {
        gold.shader = LinearGradient(CX - 11, CY - 11, CX + 11, CY + 11, GOLD, null, Shader.TileMode.CLAMP)
        canvas.drawCircle(CX, CY, 11f, gold); gold.shader = null
        ink.style = Paint.Style.FILL; ink.color = InkColors.NEWS_INK
        canvas.drawCircle(CX, CY, 11f * 0.35f, ink)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = setMeasuredDimension(WIDTH, HEIGHT)

    companion object {
        const val WIDTH = 410
        const val HEIGHT = 404
        private const val CX = 205f
        private const val CY = 200f
        private const val R = 172f
        private const val TURNER = 1.1f
        private const val MEDAL = 18f
        private const val CREAM = 0xFFF8E8C4.toInt()
        val GOLD = intArrayOf(0xFFFFE7A3.toInt(), 0xFFB8862F.toInt(), 0xFFF6D27A.toInt(), 0xFF8F6220.toInt(), 0xFFE8B95A.toInt())
    }
}

/** The still parts of the Weasleys' clock, drawn once: the gold rim, the face, the cases with their studs, the hours. */
private class WeasleyDial(private val context: Context, face: Typeface) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 11f; textAlign = Paint.Align.CENTER }
    private val hours = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = 15f; textAlign = Paint.Align.CENTER }

    fun draw(width: Int, height: Int, cx: Float, cy: Float, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        rim(canvas, cx, cy, r)
        cases(canvas, cx, cy, r)
        hourMarks(canvas, cx, cy, r - 62)
        return bmp
    }

    /** The gold rim, the face (letting a fifth of what is behind through), and its two engraved circles. */
    private fun rim(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 7f
        paint.shader = LinearGradient(cx - r, cy - r, cx + r, cy + r, WeasleyClockView.GOLD, null, Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r - 6, paint)
        paint.style = Paint.Style.FILL; paint.alpha = 204
        paint.shader = RadialGradient(cx, cy - r * 0.1f, r, intArrayOf(0xFFFBECC8.toInt(), 0xFFEFD39C.toInt(), 0xFFD9B06A.toInt()), floatArrayOf(0f, 0.7f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r - 10, paint)
        paint.shader = null; paint.alpha = 255
        paint.style = Paint.Style.STROKE; paint.strokeWidth = 1f; paint.color = InkColors.alpha(InkColors.INK, 0.6f)
        canvas.drawCircle(cx, cy, r - 14, paint); canvas.drawCircle(cx, cy, r - 40, paint)
    }

    /** Each case's name curved round the rim (péril mortel in red), and a gold stud after it. */
    private fun cases(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val studs = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(cx - r, cy - r, cx + r, cy + r, WeasleyClockView.GOLD, null, Shader.TileMode.CLAMP) }
        WeasleyCase.entries.forEach { case ->
            label.color = if (case == WeasleyCase.PERIL) InkColors.RED else InkColors.NEWS_INK
            arcText(canvas, context.getString(case.label), Ring(cx, cy, r - 27), case.angle)
            val s = Math.toRadians(case.stud.toDouble())
            canvas.drawCircle(cx + sin(s).toFloat() * (r - 27), cy - cos(s).toFloat() * (r - 27), 2.4f, studs)
        }
    }

    /** A circle to write along. */
    private class Ring(val cx: Float, val cy: Float, val r: Float)

    /** [text] along [ring], centred on [degrees] from the top, clockwise; on the lower half it reads the right way up. */
    private fun arcText(canvas: Canvas, text: String, ring: Ring, degrees: Float) {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val widths = text.map { (label.measureText(it.toString()) + 1.2f) / ring.r }
        val way = if (cos(rad) < -0.2f) -1f else 1f
        val middle = -(label.ascent() + label.descent()) / 2
        var a = rad - way * widths.sum() / 2
        text.forEachIndexed { i, ch ->
            val mid = a + way * widths[i] / 2
            canvas.save(); canvas.translate(ring.cx + ring.r * sin(mid), ring.cy - ring.r * cos(mid))
            canvas.rotate(Math.toDegrees(mid.toDouble()).toFloat() + if (way < 0) 180f else 0f)
            canvas.drawText(ch.toString(), 0f, middle, label)
            canvas.restore()
            a += way * widths[i]
        }
    }

    /** XII, III, VI and IX, and a dot for each other hour, [r] from the centre. */
    private fun hourMarks(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        hours.color = InkColors.alpha(InkColors.INK, 0.75f)
        val middle = -(hours.ascent() + hours.descent()) / 2
        paint.style = Paint.Style.FILL; paint.color = InkColors.alpha(InkColors.INK, 0.6f)
        for (i in 0 until 12) {
            val a = i * Math.PI / 6
            val x = cx + sin(a).toFloat() * r; val y = cy - cos(a).toFloat() * r
            if (i % 3 == 0) canvas.drawText(ROMAN[i / 3], x, y + middle, hours) else canvas.drawCircle(x, y, 2f, paint)
        }
    }

    private companion object {
        val ROMAN = listOf("XII", "III", "VI", "IX")
    }
}
