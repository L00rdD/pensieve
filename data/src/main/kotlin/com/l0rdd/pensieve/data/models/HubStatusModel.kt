package com.l0rdd.pensieve.data.models

import org.json.JSONObject

/** The hub's /api/status.json, the parts the home screen uses. */
data class HubStatusModel(val outlets: List<Outlet>, val covers: List<Cover>) {
    data class Outlet(val id: String, val name: String, val on: Boolean)
    data class Cover(val id: String, val name: String, val position: Int, val moving: String?)

    companion object {
        fun parse(text: String): HubStatusModel {
            val json = JSONObject(text)
            val outlets = json.optJSONArray("outlets")
            val covers = json.optJSONArray("covers")
            return HubStatusModel(
                outlets = (0 until (outlets?.length() ?: 0)).map { outlets!!.getJSONObject(it) }
                    .map { Outlet(it.getString("id"), it.optString("name", it.getString("id")), it.optBoolean("on")) },
                covers = (0 until (covers?.length() ?: 0)).map { covers!!.getJSONObject(it) }
                    .map { Cover(it.getString("id"), it.optString("name", it.getString("id")), it.optInt("position"), it.optString("moving").ifEmpty { null }.takeIf { m -> m != "null" }) },
            )
        }
    }
}
