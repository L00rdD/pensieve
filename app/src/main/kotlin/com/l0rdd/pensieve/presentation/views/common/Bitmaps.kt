package com.l0rdd.pensieve.presentation.views.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.l0rdd.pensieve.domain.entities.common.Artwork

/** Decoded artwork, the most recent kept in memory (about [maxKb] of pixels). */
class Bitmaps(maxKb: Int = 12 * 1024) {
    private val cache = object : LruCache<String, Bitmap>(maxKb) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount / 1024
    }

    fun of(artwork: Artwork?): Bitmap? {
        artwork ?: return null
        cache.get(artwork.key)?.let { return it }
        val bitmap = BitmapFactory.decodeByteArray(artwork.bytes, 0, artwork.bytes.size) ?: return null
        cache.put(artwork.key, bitmap)
        return bitmap
    }
}
