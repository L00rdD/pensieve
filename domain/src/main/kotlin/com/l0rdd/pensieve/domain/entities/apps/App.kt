package com.l0rdd.pensieve.domain.entities.apps

import com.l0rdd.pensieve.domain.entities.common.Artwork

/** An app the projector can open, with the artwork its publisher ships (TV banner, or icon). */
data class App(
    val id: AppId,
    val label: String,
    val artwork: Artwork?,
    val kind: AppKind = AppKind.APP,
)
