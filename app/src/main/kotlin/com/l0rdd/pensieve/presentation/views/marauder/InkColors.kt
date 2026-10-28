package com.l0rdd.pensieve.presentation.views.marauder

/** The Map's warm parchment and its inks. */
object InkColors {
    const val INK = 0xFF3F210B.toInt()
    const val MUTED = 0xFF74471F.toInt()
    const val RED = 0xFF9C1F12.toInt()
    const val PAPER = 0xFFF6DDA6.toInt()
    const val NEWSPRINT = 0xFFF2DFB6.toInt()
    const val NEWS_INK = 0xFF2A1608.toInt()
    const val ROOM = 0xE0F6DEAC.toInt()

    fun alpha(color: Int, a: Float): Int = (color and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255).toInt() shl 24)
}
