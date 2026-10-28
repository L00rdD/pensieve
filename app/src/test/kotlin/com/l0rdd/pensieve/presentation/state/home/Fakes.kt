package com.l0rdd.pensieve.presentation.state.home

import com.l0rdd.pensieve.domain.entities.apps.App
import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.entities.apps.Shelf
import com.l0rdd.pensieve.domain.entities.common.Artwork
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.entities.look.Look
import com.l0rdd.pensieve.domain.entities.resume.Resume
import com.l0rdd.pensieve.domain.repositories.AppRepository
import com.l0rdd.pensieve.domain.repositories.ArtworkRepository
import com.l0rdd.pensieve.domain.repositories.ClockRepository
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import com.l0rdd.pensieve.domain.repositories.LookRepository
import com.l0rdd.pensieve.domain.repositories.ResumeRepository
import com.l0rdd.pensieve.domain.repositories.ShelfRepository
import com.l0rdd.pensieve.domain.repositories.SystemRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeApps(private val apps: List<App>) : AppRepository {
    val launched = mutableListOf<AppId>()
    override fun observeApps(): Flow<List<App>> = flowOf(apps)
    override suspend fun launch(id: AppId) { launched += id }
}

class FakeShelf(initial: Shelf) : ShelfRepository {
    val state = MutableStateFlow(initial)
    override fun observeShelf(): Flow<Shelf> = state
    override suspend fun save(shelf: Shelf) { state.value = shelf }
}

class FakeResume(private val resume: Resume?) : ResumeRepository {
    val played = mutableListOf<Pair<Resume, Boolean>>()
    override fun observeResume(): Flow<Resume?> = flowOf(resume)
    override suspend fun play(resume: Resume, fromStart: Boolean) { played += resume to fromStart }
}

class FakeArtwork : ArtworkRepository {
    override suspend fun load(url: String) = Artwork(url, byteArrayOf(1))
}

class FakeHouse(private val house: House) : HouseRepository {
    val switched = mutableListOf<Pair<OutletId, Boolean>>()
    val moved = mutableListOf<Int>()
    override fun observeHouse(): Flow<House> = flowOf(house)
    override suspend fun switchOutlet(id: OutletId, on: Boolean) { switched += id to on }
    override suspend fun moveScreen(position: Int) { moved += position }
}

class FakeLook : LookRepository {
    val state = MutableStateFlow(Look.JARVIS)
    override fun observeLook(): Flow<Look> = state
    override suspend fun save(look: Look) { state.value = look }
}

class FakeClock(private val now: LocalDateTime) : ClockRepository {
    override fun observeNow(): Flow<LocalDateTime> = flowOf(now)
}

class FakeSystem : SystemRepository {
    var settings = 0
    override suspend fun openSettings() { settings++ }
}
