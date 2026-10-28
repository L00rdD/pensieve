package com.l0rdd.pensieve.dependency_injector

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.l0rdd.pensieve.domain.usecases.apps.HideApp
import com.l0rdd.pensieve.domain.usecases.apps.LaunchApp
import com.l0rdd.pensieve.domain.usecases.apps.MarkAppSeen
import com.l0rdd.pensieve.domain.usecases.apps.MoveFavorite
import com.l0rdd.pensieve.domain.usecases.apps.ObserveHomeApps
import com.l0rdd.pensieve.domain.usecases.apps.PinFavorite
import com.l0rdd.pensieve.domain.usecases.house.ObserveHouse
import com.l0rdd.pensieve.domain.usecases.house.ToggleCinemaMode
import com.l0rdd.pensieve.domain.usecases.house.ToggleOutlet
import com.l0rdd.pensieve.domain.usecases.house.ToggleScreen
import com.l0rdd.pensieve.domain.usecases.look.ChangeLook
import com.l0rdd.pensieve.domain.usecases.look.ObserveLook
import com.l0rdd.pensieve.domain.usecases.resume.LoadArtwork
import com.l0rdd.pensieve.domain.usecases.resume.ObserveResume
import com.l0rdd.pensieve.domain.usecases.resume.RestartPlayback
import com.l0rdd.pensieve.domain.usecases.resume.ResumePlayback
import com.l0rdd.pensieve.domain.usecases.system.OpenSettings
import com.l0rdd.pensieve.domain.usecases.time.ObserveTime
import com.l0rdd.pensieve.presentation.state.home.HomeEvent
import com.l0rdd.pensieve.presentation.state.home.HomeUseCases
import com.l0rdd.pensieve.presentation.state.home.HomeViewModel
import com.l0rdd.pensieve.presentation.strategies.HomeViewStrategyFactory
import com.l0rdd.pensieve.presentation.strategies.HomeViewTools
import com.l0rdd.pensieve.presentation.views.common.Bitmaps
import com.l0rdd.pensieve.presentation.views.common.Fonts
import com.l0rdd.pensieve.presentation.views.common.FrameClock

/** Builds the home screen's view model and its use cases on the app scope. */
fun injectHome(scope: AppScope): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
        HomeUseCases(
            observeLook = ObserveLook(scope.look),
            changeLook = ChangeLook(scope.look),
            observeHomeApps = ObserveHomeApps(scope.apps, scope.shelf),
            launchApp = LaunchApp(scope.apps, scope.shelf),
            pinFavorite = PinFavorite(scope.shelf),
            moveFavorite = MoveFavorite(scope.shelf),
            hideApp = HideApp(scope.shelf),
            markAppSeen = MarkAppSeen(scope.shelf),
            observeResume = ObserveResume(scope.resume),
            resumePlayback = ResumePlayback(scope.resume),
            restartPlayback = RestartPlayback(scope.resume),
            loadArtwork = LoadArtwork(scope.artwork),
            observeHouse = ObserveHouse(scope.house),
            toggleOutlet = ToggleOutlet(scope.house),
            toggleScreen = ToggleScreen(scope.house),
            toggleCinemaMode = ToggleCinemaMode(scope.house),
            observeTime = ObserveTime(scope.clock),
            openSettings = OpenSettings(scope.system),
        ),
    ) as T
}

/** Builds the factory of the looks' home views, for an activity. */
fun injectHomeViews(context: Context, clock: FrameClock, dispatch: (HomeEvent) -> Unit) =
    HomeViewStrategyFactory(HomeViewTools(context, Fonts(context), Bitmaps(), clock, dispatch))
