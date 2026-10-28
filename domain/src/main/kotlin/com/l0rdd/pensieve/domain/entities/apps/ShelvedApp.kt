package com.l0rdd.pensieve.domain.entities.apps

/** An app as the home screen shows it: new until it has been opened or arranged. */
data class ShelvedApp(val app: App, val isNew: Boolean)
