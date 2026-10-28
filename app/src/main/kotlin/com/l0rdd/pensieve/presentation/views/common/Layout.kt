package com.l0rdd.pensieve.presentation.views.common

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/** Places [view] at [x], [y] on a FrameLayout laid out in stage pixels. */
fun FrameLayout.place(view: View, x: Int, y: Int, w: Int = ViewGroup.LayoutParams.WRAP_CONTENT, h: Int = ViewGroup.LayoutParams.WRAP_CONTENT): View {
    addView(view, FrameLayout.LayoutParams(w, h).apply { leftMargin = x; topMargin = y })
    return view
}

/** A view the remote can reach, that reports OK and OK held. */
fun View.pressable(onClick: () -> Unit, onHold: (() -> Unit)? = null) {
    isFocusable = true
    isClickable = true
    setOnClickListener { onClick() }
    if (onHold != null) setOnLongClickListener { onHold(); true }
}
