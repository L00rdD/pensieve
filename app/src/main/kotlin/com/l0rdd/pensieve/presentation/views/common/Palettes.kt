package com.l0rdd.pensieve.presentation.views.common

import com.l0rdd.pensieve.domain.entities.look.Look
import org.json.JSONObject

/**
 * The looks' colours, from the house's looks.json: the reference the hub serves at
 * /theme/looks.json, of which app/src/main/assets/looks.json is a copy. Loaded once, when
 * the app starts, before any view asks for its colours.
 */
object Palettes {
    class Palette(val bg: Int, val accent: Int, val accent2: Int, val text: Int, val muted: Int, val hot: Int, val ok: Int)

    private var byId: Map<String, Palette> = emptyMap()

    fun load(json: String) {
        val looks = JSONObject(json).getJSONArray("looks")
        byId = (0 until looks.length()).map { looks.getJSONObject(it) }.associate { look ->
            val c = look.getJSONObject("colors")
            look.getString("id") to Palette(
                bg = hex(c.getString("bg")), accent = hex(c.getString("accent")), accent2 = hex(c.getString("accent2")),
                text = hex(c.getString("text")), muted = hex(c.getString("muted")), hot = hex(c.getString("hot")), ok = hex(c.getString("ok")),
            )
        }
    }

    operator fun get(look: Look): Palette = byId[look.id] ?: error("no palette for ${look.id}: load looks.json first")

    /** "#6fe3ff" as an opaque colour. */
    private fun hex(text: String): Int = text.removePrefix("#").toLong(16).toInt() or 0xFF000000.toInt()
}
