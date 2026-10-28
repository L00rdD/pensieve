package com.l0rdd.pensieve.presentation.state.home

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

/** Every use case the home screen calls. */
class HomeUseCases(
    val observeLook: ObserveLook,
    val changeLook: ChangeLook,
    val observeHomeApps: ObserveHomeApps,
    val launchApp: LaunchApp,
    val pinFavorite: PinFavorite,
    val moveFavorite: MoveFavorite,
    val hideApp: HideApp,
    val markAppSeen: MarkAppSeen,
    val observeResume: ObserveResume,
    val resumePlayback: ResumePlayback,
    val restartPlayback: RestartPlayback,
    val loadArtwork: LoadArtwork,
    val observeHouse: ObserveHouse,
    val toggleOutlet: ToggleOutlet,
    val toggleScreen: ToggleScreen,
    val toggleCinemaMode: ToggleCinemaMode,
    val observeTime: ObserveTime,
    val openSettings: OpenSettings,
)
