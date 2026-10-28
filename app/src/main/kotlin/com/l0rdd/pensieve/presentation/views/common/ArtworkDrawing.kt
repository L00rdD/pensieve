package com.l0rdd.pensieve.presentation.views.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/** Draws an app's artwork fitted in a box, centred, with rounded corners. */
object ArtworkDrawing {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun fit(canvas: Canvas, bitmap: Bitmap, box: RectF, radius: Float = 12f) {
        val scale = minOf(box.width() / bitmap.width, box.height() / bitmap.height)
        val w = bitmap.width * scale; val h = bitmap.height * scale
        val dst = RectF(box.centerX() - w / 2, box.centerY() - h / 2, box.centerX() + w / 2, box.centerY() + h / 2)
        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(dst, radius, radius, android.graphics.Path.Direction.CW) })
        canvas.drawBitmap(bitmap, null, dst, paint)
        canvas.restore()
    }
}
