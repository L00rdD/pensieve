package com.l0rdd.pensieve.presentation.views.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The time as French speakers say it ("une heure moins le quart"), and the dates the looks print. */
object FrenchTime {
    private val numbers = listOf(
        "zéro", "une", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix", "onze", "douze",
        "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf", "vingt", "vingt et une",
        "vingt-deux", "vingt-trois", "vingt-quatre", "vingt-cinq", "vingt-six", "vingt-sept", "vingt-huit", "vingt-neuf",
    )
    private val french = Locale.FRENCH

    fun inWords(hour: Int, minute: Int): String {
        val (next, minutes) = when {
            minute == 0 -> false to ""
            minute == 15 -> false to " et quart"
            minute == 30 -> false to " et demie"
            minute == 45 -> true to " moins le quart"
            minute > 30 -> true to " moins " + masculine(numbers[60 - minute])
            else -> false to " " + masculine(numbers[minute])
        }
        val h = (hour + if (next) 1 else 0) % 24
        val hours = when {
            h == 0 -> "minuit"
            h == 12 -> "midi"
            h % 12 == 1 -> "une heure"
            else -> numbers[h % 12] + " heures"
        }
        return hours + minutes
    }

    fun hm(now: LocalDateTime): String = "%02d:%02d".format(now.hour, now.minute)

    /** "mercredi 28 octobre" */
    fun longDate(now: LocalDateTime): String = now.format(DateTimeFormatter.ofPattern("EEEE d MMMM", french))

    /** "MER. 28 OCT." */
    fun shortDate(now: LocalDateTime): String = now.format(DateTimeFormatter.ofPattern("EEE d MMM", french)).uppercase(french)

    /** Minutes as they are counted on a film: 72 min → "1:12". */
    fun clock(totalMinutes: Long): String = "%d:%02d".format(totalMinutes / 60, totalMinutes % 60)

    private fun masculine(word: String) = word.replace(Regex("une$"), "un")
}
