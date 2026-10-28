package com.l0rdd.pensieve.dependency_injector

import android.content.Context
import com.l0rdd.pensieve.BuildConfig
import com.l0rdd.pensieve.data.datasources.AndroidSystemSettingsDataSource
import com.l0rdd.pensieve.data.datasources.BinderJmgoMiddlewareDataSource
import com.l0rdd.pensieve.data.datasources.PackageManagerAppsDataSource
import com.l0rdd.pensieve.data.datasources.SharedPreferencesDataSource
import com.l0rdd.pensieve.data.datasources.UrlConnectionHttpDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import com.l0rdd.pensieve.data.mappers.HubStatusMapper
import com.l0rdd.pensieve.data.mappers.InstalledAppMapper
import com.l0rdd.pensieve.data.repositories.apps.PackageManagerAppRepository
import com.l0rdd.pensieve.data.repositories.apps.PreferencesShelfRepository
import com.l0rdd.pensieve.data.repositories.house.HubHouseRepository
import com.l0rdd.pensieve.data.repositories.look.PreferencesLookRepository
import com.l0rdd.pensieve.data.repositories.resume.FeedResumeRepository
import com.l0rdd.pensieve.data.repositories.resume.HttpArtworkRepository
import com.l0rdd.pensieve.data.repositories.scenes.JmgoSceneRepository
import com.l0rdd.pensieve.data.repositories.system.AndroidSystemRepository
import com.l0rdd.pensieve.data.repositories.time.SystemClockRepository

private const val STORE = "com.android.vending"
private const val SCREEN = "screen"

/** The action that opens the quick panel (see the manifest). */
const val QUICK_PANEL = "com.l0rdd.pensieve.QUICK_PANEL"

/** The favourites of a first run, those that are installed. */
private val DEFAULT_FAVORITES = listOf(
    "com.netflix.ninja", "org.jellyfin.androidtv", "com.canal.android.canal",
    "com.google.android.youtube.tv", "com.disney.disneyplus", "com.amazon.amazonvideo.livingroom",
)

/** Builds the app scope: the sources and the repositories over them. */
fun injectAppScope(context: Context): AppScope {
    val app = context.applicationContext
    val packages = PackageManagerAppsDataSource(app)
    val prefs = SharedPreferencesDataSource(app)
    val http = UrlConnectionHttpDataSource()
    return AppScope(
        apps = PackageManagerAppRepository(packages, InstalledAppMapper(STORE)),
        shelf = PreferencesShelfRepository(prefs, packages, DEFAULT_FAVORITES),
        resume = FeedResumeRepository(http, packages, FeedResumeMapper(), BuildConfig.FEED_URL),
        artwork = HttpArtworkRepository(http, BuildConfig.JELLYFIN_URL),
        house = HubHouseRepository(http, HubStatusMapper(BuildConfig.CINEMA_OUTLETS.split(',').toSet(), SCREEN), BuildConfig.HUB_URL, SCREEN),
        look = PreferencesLookRepository(prefs),
        clock = SystemClockRepository(),
        system = AndroidSystemRepository(packages, QUICK_PANEL, app.packageName),
        scenes = JmgoSceneRepository(AndroidSystemSettingsDataSource(app), BinderJmgoMiddlewareDataSource(app)),
    )
}
