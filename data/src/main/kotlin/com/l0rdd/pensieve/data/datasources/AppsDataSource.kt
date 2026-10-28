package com.l0rdd.pensieve.data.datasources

import android.content.Intent
import kotlinx.coroutines.flow.Flow

/** The apps a TV home screen can open, their artwork, and starting them. */
interface AppsDataSource {
    /** Every app with a TV or a phone launcher entry, but this home screen; TV banners preferred. */
    fun query(): List<InstalledApp>

    /** The package names of [query], without loading any artwork. */
    fun packages(): List<String>

    /** Fires once now, then whenever an app is installed, removed or updated. */
    fun changes(): Flow<Unit>

    fun launch(packageName: String)

    /** Starts [intent] from outside an activity; false when nothing answers it. */
    fun start(intent: Intent): Boolean
}
