package com.l0rdd.pensieve.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelsTest {
    @Test
    fun `hub status keeps outlets and covers`() {
        val m = HubStatusModel.parse("""{"outlets":[{"id":"basketball-neon","name":"Néon basket","on":true}],
            "covers":[{"id":"screen","name":"Écran","position":100,"moving":null}]}""")
        assertEquals(HubStatusModel.Outlet("basketball-neon", "Néon basket", true), m.outlets.single())
        assertEquals(HubStatusModel.Cover("screen", "Écran", 100, null), m.covers.single())
    }

    @Test
    fun `the feed's resume list, with nulls where the feed has none`() {
        val list = FeedResumeModel.parseList("""{"resume":[{"id":"abc","type":"movie","title":"Dune","series":null,"season":null,
            "episode":null,"year":2024,"overview":"…","genres":["Science Fiction"],"runtime":9960,"position":4324,
            "poster":"/Items/abc/Images/Primary","backdrop":null}]}""")
        val r = list.single()
        assertEquals("Dune", r.title); assertNull(r.series); assertNull(r.backdrop)
        assertEquals(2024, r.year); assertEquals(9960, r.runtime); assertEquals(4324, r.position)
    }

    @Test
    fun `a feed without a resume list resumes nothing`() {
        assertEquals(emptyList<FeedResumeModel>(), FeedResumeModel.parseList("""{"playing":[]}"""))
    }

    @Test
    fun `the shelf goes to json and back`() {
        val shelf = ShelfModel(listOf("b", "a"), setOf("c"), setOf("a", "b", "c"))
        assertEquals(shelf, ShelfModel.parse(shelf.toJson()))
    }
}
