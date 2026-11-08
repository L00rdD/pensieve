package com.l0rdd.pensieve.domain.usecases.apps

import com.l0rdd.pensieve.domain.entities.apps.AppId
import com.l0rdd.pensieve.domain.fakes.FakeAppRepository
import com.l0rdd.pensieve.domain.fakes.FakeProjectorRepository
import com.l0rdd.pensieve.domain.fakes.app
import com.l0rdd.pensieve.domain.usecases.NoParam
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CloseAppsTest {

    @Test
    fun `closes every installed app at once`() = runTest {
        val projector = FakeProjectorRepository()
        CloseApps(FakeAppRepository(listOf(app("com.netflix.ninja"), app("org.videolan.vlc"))), projector)(NoParam)
        assertEquals(listOf(listOf(AppId("com.netflix.ninja"), AppId("org.videolan.vlc"))), projector.closed)
    }
}
