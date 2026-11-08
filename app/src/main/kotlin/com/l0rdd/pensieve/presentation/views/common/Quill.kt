package com.l0rdd.pensieve.presentation.views.common

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView

/**
 * Writes words on the hint [line] as by a quill, in [ink] and italic, a letter every 22 ms, leaves
 * them 1.6 s, then gives the line back to what it said ([after]). On the view's own timers, not the
 * frame clock, which may be resting.
 */
class Quill(private val line: TextView, private val ink: Int, private val after: () -> CharSequence) : Saying, Runnable {
    private var words = ""
    private var written = 0
    private val done = Runnable { writing = false; line.text = after() }

    /** True while words are written or left on the line: hints wait. */
    var writing = false
        private set

    override fun say(words: String) {
        line.removeCallbacks(this); line.removeCallbacks(done)
        this.words = words; written = 0; writing = true
        line.post(this)
    }

    override fun run() {
        written++
        line.text = SpannableString(words.take(written)).apply {
            setSpan(ForegroundColorSpan(ink), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.ITALIC), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if (written < words.length) line.postDelayed(this, LETTER) else line.postDelayed(done, KEPT)
    }

    private companion object {
        const val LETTER = 22L
        const val KEPT = 1600L
    }
}
