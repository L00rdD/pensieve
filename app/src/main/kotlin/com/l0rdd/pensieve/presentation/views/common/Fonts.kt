package com.l0rdd.pensieve.presentation.views.common

import android.content.Context
import android.graphics.Typeface
import com.l0rdd.pensieve.R

/** The looks' typefaces (all under the SIL Open Font License, see licenses/). */
class Fonts(context: Context) {
    val michroma: Typeface = context.resources.getFont(R.font.michroma)
    val rajdhani: Typeface = context.resources.getFont(R.font.rajdhani_medium)
    val rajdhaniSemi: Typeface = context.resources.getFont(R.font.rajdhani_semibold)
    val rajdhaniBold: Typeface = context.resources.getFont(R.font.rajdhani_bold)
    val fell: Typeface = context.resources.getFont(R.font.im_fell)
    val fellItalic: Typeface = context.resources.getFont(R.font.im_fell_italic)
}
