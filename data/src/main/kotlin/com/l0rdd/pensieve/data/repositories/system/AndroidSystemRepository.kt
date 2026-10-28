package com.l0rdd.pensieve.data.repositories.system

import android.content.Intent
import android.provider.Settings
import com.l0rdd.pensieve.data.datasources.AppsDataSource
import com.l0rdd.pensieve.domain.entities.system.SystemScreen
import com.l0rdd.pensieve.domain.repositories.SystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The projector's screens: Android TV's settings, JMGO's keystone correction, and the quick panel
 * of this app ([quickPanelAction], answered by its own activity). A JMGO screen that does not answer
 * falls back on the settings.
 */
class AndroidSystemRepository(
    private val apps: AppsDataSource,
    private val quickPanelAction: String,
    private val ownPackage: String,
) : SystemRepository {

    override suspend fun open(screen: SystemScreen) = withContext(Dispatchers.Main) {
        val intent = when (screen) {
            SystemScreen.SETTINGS -> Intent(Settings.ACTION_SETTINGS)
            SystemScreen.PICTURE -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            SystemScreen.NETWORK -> Intent(Settings.ACTION_WIFI_SETTINGS)
            SystemScreen.KEYSTONE -> Intent().setClassName(TV_SETTINGS, "com.jmgo.jmgosettinglib.module.keystone.ManualFittingActivity")
            SystemScreen.QUICK_PANEL -> Intent(quickPanelAction).setPackage(ownPackage)
        }
        if (!apps.start(intent) && screen != SystemScreen.QUICK_PANEL) apps.start(Intent(Settings.ACTION_SETTINGS))
        Unit
    }

    private companion object {
        const val TV_SETTINGS = "com.android.tv.settings"
    }
}
