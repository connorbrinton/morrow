package family.morrow

import android.content.Context
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.time.LocalDate

data class Lesson(val date: LocalDate, val title: String, val reference: String, val scene: String,
    val discover: String, val explain: String, val younger: String, val older: String, val activity: String, val remember: String)
data class Week(val start: LocalDate, val title: String, val assignment: String, val source: String, val days: List<Lesson>)
data class Pack(val weeks: List<Week>, val translations: Map<String, Map<String, String>>) {
    fun today(date: LocalDate): Lesson = weeks.flatMap { it.days }.minBy { kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(it.date, date)) }
}
object Content {
    @Suppress("UNCHECKED_CAST")
    fun parse(text: String): Pack {
        require(text.length <= 2_000_000) { "Content pack is too large" }
        val options = LoaderOptions().apply { codePointLimit = 2_000_000; maxAliasesForCollections = 10; isAllowDuplicateKeys = false }
        val root = Yaml(SafeConstructor(options)).load<Map<String, Any>>(text)
        require((root["schema"] as Number).toInt() == 1) { "Unsupported content version" }
        fun Map<String, Any>.str(k: String) = (this[k] as String).also { require(it.isNotBlank() && it.length < 12000) { "Invalid $k" } }
        val weeks = (root["weeks"] as List<Map<String, Any>>).map { w ->
            val start = LocalDate.parse(w.str("start"))
            require(start.dayOfWeek == java.time.DayOfWeek.MONDAY)
            val days = (w["days"] as List<Map<String, Any>>).map { d -> Lesson(LocalDate.parse(d.str("date")), d.str("title"), d.str("reference"), d.str("scene"), d.str("discover"), d.str("explain"), d.str("younger"), d.str("older"), d.str("activity"), d.str("remember")) }
            require(days.size == 7 && days.map { it.date } == (0L..6L).map { start.plusDays(it) }) { "Each week needs seven consecutive days" }
            Week(start, w.str("title"), w.str("assignment"), w.str("source"), days)
        }
        require(weeks.isNotEmpty() && weeks.flatMap { it.days }.map { it.date }.distinct().size == weeks.size * 7)
        val translations = root["translations"] as Map<String, Map<String, String>>
        require(weeks.flatMap { it.days }.all { !translations["KJV"]?.get(it.reference).isNullOrBlank() }) { "Missing KJV passage" }
        require(translations.values.all { entries -> entries.all { it.value.isNotBlank() && it.value.length < 12000 } })
        return Pack(weeks.sortedBy { it.start }, translations)
    }
    fun load(context: Context): Pack = parse(
        context.assets.open("content.yaml").bufferedReader().use { it.readText() }
    )
}
