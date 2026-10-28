package com.l0rdd.pensieve.architecture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reads the sources and fails on every line that breaks a layer rule, with its file, line and
 * rule. No exception: a breach gets fixed.
 */
class LayersTest {
    private val root = File("..").canonicalFile
    private val app = "com/l0rdd/pensieve"

    private data class Rule(val what: String, val pattern: Regex)

    private fun sources(dir: String) = File(root, dir).walkTopDown().filter { it.extension == "kt" }.toList()

    private fun breaches(files: List<File>, rules: List<Rule>): List<String> = files.flatMap { file ->
        file.readLines().mapIndexedNotNull { i, line ->
            rules.firstOrNull { it.pattern.containsMatchIn(line) }?.let { "${file.relativeTo(root)}:${i + 1}: ${it.what} — ${line.trim()}" }
        }
    }

    private val readsASource = listOf(
        Rule("reads a source outside data", Regex("""HttpURLConnection|SharedPreferences|PackageManager|BroadcastReceiver|getSharedPreferences|startActivity\(""")),
        Rule("reads the clock outside data", Regex("""LocalDateTime\.now\(|Instant\.now\(|System\.currentTimeMillis""")),
    )

    @Test
    fun `the domain knows neither Android, nor data, nor presentation, nor the injector`() {
        val found = breaches(sources("domain/src/main"), listOf(
            Rule("domain imports Android", Regex("""^import (android|androidx)\.""")),
            Rule("domain imports data", Regex("""^import com\.l0rdd\.pensieve\.data""")),
            Rule("domain imports presentation", Regex("""^import com\.l0rdd\.pensieve\.presentation""")),
            Rule("domain imports the injector", Regex("""^import com\.l0rdd\.pensieve\.dependency_injector""")),
            Rule("domain imports resources", Regex("""^import com\.l0rdd\.pensieve\.R\b""")),
            Rule("domain parses or fetches", Regex("""^import (org\.json|java\.net)""")),
        ) + readsASource)
        assertTrue(found.joinToString("\n"), found.isEmpty())
    }

    @Test
    fun `data knows neither presentation, nor the injector, nor texts, nor widgets`() {
        val found = breaches(sources("data/src/main"), listOf(
            Rule("data imports presentation", Regex("""^import com\.l0rdd\.pensieve\.presentation""")),
            Rule("data imports the injector", Regex("""^import com\.l0rdd\.pensieve\.dependency_injector""")),
            Rule("data imports texts or resources", Regex("""^import com\.l0rdd\.pensieve\.R\b""")),
            Rule("data imports widgets", Regex("""^import android\.(widget|view)\.""")),
        ))
        assertTrue(found.joinToString("\n"), found.isEmpty())
    }

    @Test
    fun `presentation does not import data and reads no source`() {
        val found = breaches(sources("app/src/main/kotlin/$app/presentation"), listOf(
            Rule("presentation imports data", Regex("""^import com\.l0rdd\.pensieve\.data""")),
        ) + readsASource)
        assertTrue(found.joinToString("\n"), found.isEmpty())
    }

    @Test
    fun `the injector is called only by the activity and the application`() {
        val files = sources("app/src/main/kotlin/$app/presentation").filter { it.name != "HomeActivity.kt" }
        val found = breaches(files, listOf(Rule("calls the injector", Regex("""^import com\.l0rdd\.pensieve\.dependency_injector"""))))
        assertTrue(found.joinToString("\n"), found.isEmpty())
    }

    @Test
    fun `the rules see the sources`() {
        assertTrue(sources("domain/src/main").size > 20 && sources("data/src/main").size > 10 && sources("app/src/main").size > 20)
    }
}
