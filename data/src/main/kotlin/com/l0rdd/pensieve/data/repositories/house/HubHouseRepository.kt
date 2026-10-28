package com.l0rdd.pensieve.data.repositories.house

import com.l0rdd.pensieve.data.datasources.HttpDataSource
import com.l0rdd.pensieve.data.mappers.HubStatusMapper
import com.l0rdd.pensieve.data.models.HubStatusModel
import com.l0rdd.pensieve.domain.entities.house.House
import com.l0rdd.pensieve.domain.entities.house.OutletId
import com.l0rdd.pensieve.domain.repositories.HouseRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The house through the bifrost hub ([hubUrl]): its status read every [pollMs], and again right
 * after a command so the change shows at once. A hub that does not answer gives an unreachable house.
 */
class HubHouseRepository(
    private val http: HttpDataSource,
    private val mapper: HubStatusMapper,
    private val hubUrl: String,
    private val screenId: String,
    private val pollMs: Long = 3_000,
) : HouseRepository {
    private val refresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeHouse(): Flow<House> = channelFlow {
        launch { refresh.collect { send(fetch()) } }
        while (isActive) {
            send(fetch())
            delay(pollMs)
        }
    }

    override suspend fun switchOutlet(id: OutletId, on: Boolean) {
        http.postJson("$hubUrl/api/outlets/${id.value}", """{"on": $on}""")
        refresh.tryEmit(Unit)
    }

    override suspend fun moveScreen(position: Int) {
        http.postJson("$hubUrl/api/covers/$screenId", """{"position": $position}""")
        refresh.tryEmit(Unit)
    }

    private suspend fun fetch(): House =
        runCatching { mapper.toHouse(HubStatusModel.parse(http.getText("$hubUrl/api/status.json"))) }.getOrDefault(House.UNKNOWN)
}
