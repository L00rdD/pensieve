package com.l0rdd.pensieve.presentation.views.common

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FrenchTimeTest {

    @Test
    fun `the hours as they are said, midnight and noon by their names`() {
        assertEquals("minuit", FrenchTime.inWords(0, 0))
        assertEquals("midi", FrenchTime.inWords(12, 0))
        assertEquals("une heure", FrenchTime.inWords(13, 0))
        assertEquals("une heure", FrenchTime.inWords(1, 0))
        assertEquals("neuf heures", FrenchTime.inWords(21, 0))
    }

    @Test
    fun `quarters and halves`() {
        assertEquals("deux heures et quart", FrenchTime.inWords(2, 15))
        assertEquals("deux heures et demie", FrenchTime.inWords(14, 30))
        assertEquals("trois heures moins le quart", FrenchTime.inWords(2, 45))
        assertEquals("minuit moins le quart", FrenchTime.inWords(23, 45))
        assertEquals("une heure moins le quart", FrenchTime.inWords(12, 45))
    }

    @Test
    fun `minutes after the hour, then before the next one, in the masculine`() {
        assertEquals("neuf heures cinq", FrenchTime.inWords(9, 5))
        assertEquals("minuit vingt et un", FrenchTime.inWords(0, 21))
        assertEquals("une heure un", FrenchTime.inWords(1, 1))
        assertEquals("dix heures moins vingt-six", FrenchTime.inWords(21, 34))
        assertEquals("midi moins un", FrenchTime.inWords(11, 59))
        assertEquals("une heure moins vingt-neuf", FrenchTime.inWords(0, 31))
    }

    @Test
    fun `the digits, the dates and a film's minutes`() {
        val now = LocalDateTime.of(2026, 10, 28, 9, 5)
        assertEquals("09:05", FrenchTime.hm(now))
        assertEquals("mercredi 28 octobre", FrenchTime.longDate(now))
        assertEquals("MER. 28 OCT.", FrenchTime.shortDate(now))
        assertEquals("1:12", FrenchTime.clock(72))
        assertEquals("0:05", FrenchTime.clock(5))
    }
}
