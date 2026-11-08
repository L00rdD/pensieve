package com.l0rdd.pensieve.data.repositories.apps

import com.l0rdd.pensieve.data.fakes.FakeAppsDataSource
import com.l0rdd.pensieve.data.mappers.InstalledAppMapper
import com.l0rdd.pensieve.domain.entities.apps.AppId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PackageManagerAppRepositoryTest {

    @Test
    fun `the installed apps are the packages the home screen can open`() = runTest {
        val repository = PackageManagerAppRepository(FakeAppsDataSource(listOf("com.netflix.ninja", "org.videolan.vlc")), InstalledAppMapper("com.android.vending"))
        assertEquals(listOf(AppId("com.netflix.ninja"), AppId("org.videolan.vlc")), repository.installed())
    }
}
