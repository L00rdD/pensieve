package com.l0rdd.pensieve.domain.entities.house

/**
 * What can be asked of the projection screen: move it, stop it, or tell the hub where it really is
 * when it missed a code ([ROLLED_UP], [OPEN]: nothing moves, the hub takes 0 % or 100 %).
 */
enum class ScreenCommand { RAISE, STOP, LOWER, ROLLED_UP, OPEN }
