package com.l0rdd.pensieve.presentation.views.common

import android.view.Choreographer

/**
 * Drives the looks' ambient animations at [fps] at most, on the display's frames, and stops
 * entirely when the home screen is not shown: an app in front costs nothing.
 */
class FrameClock(private val fps: Int = 30) : Choreographer.FrameCallback {
    private val listeners = mutableListOf<(seconds: Float, dt: Float) -> Unit>()
    private var running = false
    private var start = 0L
    private var last = 0L

    fun add(listener: (seconds: Float, dt: Float) -> Unit) { listeners += listener }

    fun clear() { listeners.clear() }

    fun start() {
        if (running) return
        running = true; last = 0L
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        if (start == 0L) start = frameTimeNanos
        if (last == 0L || frameTimeNanos - last >= 1_000_000_000L / fps - 2_000_000L) {
            val dt = if (last == 0L) 0f else (frameTimeNanos - last) / 1e9f
            last = frameTimeNanos
            val t = (frameTimeNanos - start) / 1e9f
            listeners.forEach { it(t, dt.coerceAtMost(0.1f)) }
        }
        Choreographer.getInstance().postFrameCallback(this)
    }
}
