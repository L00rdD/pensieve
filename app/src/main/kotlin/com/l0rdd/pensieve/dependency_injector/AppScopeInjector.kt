package com.l0rdd.pensieve.dependency_injector

import android.content.Context
import com.l0rdd.pensieve.BuildConfig
import com.l0rdd.pensieve.data.datasources.AndroidSystemSettingsDataSource
import com.l0rdd.pensieve.data.datasources.BinderJmgoMiddlewareDataSource
import com.l0rdd.pensieve.data.datasources.PackageManagerAppsDataSource
import com.l0rdd.pensieve.data.datasources.SharedPreferencesDataSource
import com.l0rdd.pensieve.data.datasources.StreamingFeedDataSource
import com.l0rdd.pensieve.data.datasources.UrlConnectionHttpDataSource
import com.l0rdd.pensieve.data.mappers.FeedResumeMapper
import com.l0rdd.pensieve.data.mappers.HubStatusMapper
import com.l0rdd.pensieve.data.mappers.InstalledAppMapper
import com.l0rdd.pensieve.data.repositories.apps.PackageManagerAppRepository
import com.l0rdd.pensieve.data.repositories.apps.PreferencesShelfRepository
import com.l0rdd.pensieve.data.repositories.house.HubHouseRepository
import com.l0rdd.pensieve.data.repositories.look.FeedLookRepository
import com.l0rdd.pensieve.data.repositories.resume.FeedResumeRepository
import com.l0rdd.pensieve.data.repositories.resume.HttpArtworkRepository
import com.l0rdd.pensieve.data.repositories.scenes.JmgoSceneRepository
import com.l0rdd.pensieve.data.repositories.system.AndroidSystemRepository
import com.l0rdd.pensieve.data.repositories.time.SystemClockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private const val STORE = "com.android.vending"
private const val SCREEN = "screen"

/** The action that opens the quick panel (see the manifest). */
const val QUICK_PANEL = "com.l0rdd.pensieve.QUICK_PANEL"

/** The favourites of a first run, those that are installed. */
private val DEFAULT_FAVORITES = listOf(
    "com.netflix.ninja", "org.jellyfin.androidtv", "com.canal.android.canal",
    "com.google.android.youtube.tv", "com.disney.disneyplus", "com.amazon.amazonvideo.livingroom",
)

/** Builds the app scope: the sources and the repositories over them; [scope] outlives every screen, for the feed's one connection. */
fun injectAppScope(context: Context, scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)): AppScope {
    val app = context.applicationContext
    val packages = PackageManagerAppsDataSource(app)
    val prefs = SharedPreferencesDataSource(app)
    val http = UrlConnectionHttpDataSource()
    val house = BuildConfig.FEED_URL.substringBeforeLast('/')
    val feed = StreamingFeedDataSource(http, BuildConfig.FEED_URL, "$house/screen/events", scope)
    return AppScope(
        apps = PackageManagerAppRepository(packages, InstalledAppMapper(STORE)),
        shelf = PreferencesShelfRepository(prefs, packages, DEFAULT_FAVORITES),
        resume = FeedResumeRepository(feed, packages, FeedResumeMapper()),
        artwork = HttpArtworkRepository(http, BuildConfig.JELLYFIN_URL),
        house = HubHouseRepository(http, HubStatusMapper(BuildConfig.CINEMA_OUTLETS.split(',').toSet(), SCREEN), BuildConfig.HUB_URL, SCREEN),
        look = FeedLookRepository(feed, http, prefs, "$house/screen/settings"),
        clock = SystemClockRepository(),
        system = AndroidSystemRepository(packages, QUICK_PANEL, app.packageName),
        scenes = JmgoSceneRepository(AndroidSystemSettingsDataSource(app), BinderJmgoMiddlewareDataSource(app)),
    )
}
