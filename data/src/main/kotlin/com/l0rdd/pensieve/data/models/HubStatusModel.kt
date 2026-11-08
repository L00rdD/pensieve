package com.l0rdd.pensieve.data.models

import org.json.JSONArray
import org.json.JSONObject

/** The hub's /api/status.json, the parts the home screen uses. */
data class HubStatusModel(val outlets: List<Outlet>, val covers: List<Cover>, val presences: List<Presence> = emptyList()) {
    /** An outlet; [next] is its schedule's next switch, its time as the hub writes it (`2026-11-08T23:00+01:00`). */
    data class Outlet(val id: String, val name: String, val on: Boolean, val next: Next? = null)
    data class Next(val at: String, val on: Boolean)
    data class Cover(val id: String, val name: String, val position: Int, val moving: String?, val target: Int? = null)
    /** Someone at the desk; [idle] in seconds, null when the hub has not heard from it. */
    data class Presence(val id: String, val name: String, val outlet: String?, val present: Boolean, val idle: Long?)

    companion object {
        fun parse(text: String): HubStatusModel {
            val json = JSONObject(text)
            return HubStatusModel(
                outlets = objects(json.optJSONArray("outlets")).map { outlet(it) },
                covers = objects(json.optJSONArray("covers")).map { cover(it) },
                presences = objects(json.optJSONArray("presences")).map { presence(it) },
            )
        }

        private fun objects(array: JSONArray?): List<JSONObject> = (0 until (array?.length() ?: 0)).map { array!!.getJSONObject(it) }

        private fun outlet(json: JSONObject) = Outlet(
            json.getString("id"), json.optString("name", json.getString("id")), json.optBoolean("on"),
            json.optJSONObject("next")?.let { Next(it.getString("at"), it.optBoolean("on")) },
        )

        /** A cover; the hub writes null for no move and no target. */
        private fun cover(json: JSONObject) = Cover(
            json.getString("id"), json.optString("name", json.getString("id")), json.optInt("position"),
            json.optString("moving").ifEmpty { null }.takeIf { it != "null" },
            if (json.isNull("target")) null else json.optInt("target"),
        )

        private fun presence(json: JSONObject) = Presence(
            json.getString("id"), json.optString("name", json.getString("id")),
            if (json.isNull("outlet")) null else json.optString("outlet").ifEmpty { null },
            json.optBoolean("present"),
            if (json.isNull("idle") || !json.has("idle")) null else json.optDouble("idle").toLong(),
        )
    }
}
