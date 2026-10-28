package com.l0rdd.pensieve.presentation.strategies

import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.MenuStyle

/** Gives the menus and the quick panel the colours and type of one look. */
abstract class MenuStyleStrategy {
    abstract fun execute(): MenuStyle
}

class JarvisMenuStyleStrategy(private val fonts: Fonts) : MenuStyleStrategy() {
    override fun execute() = MenuStyle(fonts.rajdhaniSemi, 0xFFE4F8FF.toInt(), 0xFFFFC857.toInt(), 0xF2041019.toInt(), 0xB3000000.toInt(), 30f)
}

class CyberpunkMenuStyleStrategy(private val fonts: Fonts) : MenuStyleStrategy() {
    override fun execute() = MenuStyle(fonts.rajdhaniBold, 0xFFF4ECE7.toInt(), 0xFFF3E600.toInt(), 0xF20B0507.toInt(), 0xB3000000.toInt(), 30f)
}

class MarauderMenuStyleStrategy(private val fonts: Fonts) : MenuStyleStrategy() {
    override fun execute() = MenuStyle(fonts.fell, 0xFF3F210B.toInt(), 0xFF9C1F12.toInt(), 0xF5F2DFB6.toInt(), 0x80461A05.toInt(), 32f, upper = false)
}

/** Picks the style of a look. */
class MenuStyleStrategyFactory(private val fonts: Fonts) {
    fun create(look: Look): MenuStyleStrategy = when (look) {
        Look.JARVIS -> JarvisMenuStyleStrategy(fonts)
        Look.CYBERPUNK -> CyberpunkMenuStyleStrategy(fonts)
        Look.MARAUDER -> MarauderMenuStyleStrategy(fonts)
    }
}
