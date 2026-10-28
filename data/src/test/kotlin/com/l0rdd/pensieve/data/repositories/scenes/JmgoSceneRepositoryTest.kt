package com.l0rdd.pensieve.data.repositories.scenes

import com.l0rdd.pensieve.data.datasources.JmgoMiddlewareDataSource
import com.l0rdd.pensieve.data.datasources.SystemSettingsDataSource
import com.l0rdd.pensieve.domain.entities.scenes.Scene
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class JmgoSceneRepositoryTest {
    private class Settings(val values: Map<String, String>) : SystemSettingsDataSource {
        override fun read(key: String) = values[key]
        override fun changes(vararg keys: String): Flow<Unit> = flowOf(Unit)
    }
    private class Middleware : JmgoMiddlewareDataSource {
        val set = mutableListOf<Pair<Int, Int>>()
        override suspend fun set(key: Int, value: Int): Int { set += key to value; return 0 }
    }

    private val memory = """[{"id":2,"sceneName":"Chambre","recentUsageTime":2},{"id":1,"sceneName":"Salon ","spaceName":"客厅","recentUsageTime":1}]"""

    @Test
    fun `the saved scenes by id, their names trimmed, the applied one marked`() = runTest {
        val scenes = JmgoSceneRepository(Settings(mapOf("jmgo_display_memory" to memory, "jmgo_display_memory_current_id" to "1")), Middleware()).observeScenes().first()
        assertEquals(listOf(Scene(1, "Salon", true), Scene(2, "Chambre", false)), scenes)
    }

    @Test
    fun `no setting gives no scene`() = runTest {
        assertEquals(emptyList<Scene>(), JmgoSceneRepository(Settings(emptyMap()), Middleware()).observeScenes().first())
    }

    @Test
    fun `applying asks the middleware to apply that memory`() = runTest {
        val middleware = Middleware()
        JmgoSceneRepository(Settings(emptyMap()), middleware).apply(2)
        assertEquals(listOf(326 to 2), middleware.set)
    }
}
