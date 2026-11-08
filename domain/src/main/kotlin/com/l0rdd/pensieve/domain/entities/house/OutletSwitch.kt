package com.l0rdd.pensieve.domain.entities.house

import java.time.LocalTime

/** The next switch an outlet's schedule makes: [on] or off, at [at] (the hub's local time). */
data class OutletSwitch(val at: LocalTime, val on: Boolean)
