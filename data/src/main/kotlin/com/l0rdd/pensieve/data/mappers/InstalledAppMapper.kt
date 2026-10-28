package com.l0rdd.pensieve.data.mappers

import com.l0rdd.pensieve.data.datasources.InstalledApp
import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.AppKind
import com.l0rdd.pensieve.domain.entities.common.Artwork

/** Package manager entries to apps; [storePackage] is the store's. */
class InstalledAppMapper(private val storePackage: String) {
    fun toApp(app: InstalledApp) = App(
        id = AppId(app.packageName),
        label = app.label,
        artwork = app.artwork?.let { Artwork("app:${app.packageName}:${it.size}", it) },
        kind = if (app.packageName == storePackage) AppKind.STORE else AppKind.APP,
    )
}
