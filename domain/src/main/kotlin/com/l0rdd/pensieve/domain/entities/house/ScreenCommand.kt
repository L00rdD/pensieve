package com.l0rdd.pensieve.domain.entities.house

/**
 * What can be asked of the projection screen: send it all the way up or down, stop it, push the
 * remote's own ▲ / ▼ whatever the hub reckons ([PRESS_UP], [PRESS_DOWN], held until a [STOP]), or
 * tell the hub where it really is when it missed a code ([ROLLED_UP], [OPEN]: nothing moves).
 */
enum class ScreenCommand { RAISE, STOP, LOWER, PRESS_UP, PRESS_DOWN, ROLLED_UP, OPEN }
