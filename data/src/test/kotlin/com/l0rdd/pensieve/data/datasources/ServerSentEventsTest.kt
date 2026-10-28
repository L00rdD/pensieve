package com.l0rdd.pensieve.data.datasources

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerSentEventsTest {
    @Test
    fun `events are the data lines between blank lines, comments skipped`() {
        val text = ": keepalive\n\ndata: {\"a\": 1}\n\nevent: x\ndata: first\ndata: second\n\ndata:last"
        assertEquals(listOf("{\"a\": 1}", "first\nsecond", "last"), ServerSentEvents.read(text.reader().buffered()).toList())
    }
}
