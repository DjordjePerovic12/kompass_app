package llc.bokadev.kompass.core.util

import llc.bokadev.kompass.domain.model.Destination
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Consolidates what used to be 4 near-identical `Instant.parse(...).toLocalDateTime(...)`
 * extraction helpers duplicated across `HomeScreenContent.kt`. Month abbreviations are a real
 * lookup table per language — the code this replaced called itself "localized" but actually
 * used `Month.name` (English only) regardless of the app's current language.
 */
private val monthAbbreviations = mapOf(
    "en" to listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
    "fr" to listOf("janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc."),
    "tr" to listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara"),
    "es" to listOf("ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic."),
    "de" to listOf("Jan.", "Feb.", "März", "Apr.", "Mai", "Juni", "Juli", "Aug.", "Sep.", "Okt.", "Nov.", "Dez.")
)

@OptIn(ExperimentalTime::class)
private fun String.toLocalDateTimeOrNull() = runCatching {
    Instant.parse(this).toLocalDateTime(Destination.timeZone)
}.getOrNull()

fun String.toDayOfMonth(): String = toLocalDateTimeOrNull()?.date?.day?.toString() ?: "--"

fun String.toMonthAbbrev(lang: String): String {
    val dateTime = toLocalDateTimeOrNull() ?: return "---"
    val months = monthAbbreviations[lang] ?: monthAbbreviations.getValue("en")
    return months.getOrElse(dateTime.date.month.number - 1) { "---" }
}

/** "DD Mon" — e.g. "05 Oct". Used by the event date tag (spec §3.5). */
fun String.toDateTag(lang: String): String =
    "${toDayOfMonth().padStart(2, '0')} ${toMonthAbbrev(lang)}"

/** "HH:MM" in Kotor time (the destination, not the device), or null if unparsable. */
fun String.toClockTime(): String? = toLocalDateTimeOrNull()?.time?.let {
    "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
}

/** "D. Mon" (no leading zero) — the existing notice-meta style. */
fun String.toNoticeDate(lang: String): String? {
    val dateTime = toLocalDateTimeOrNull() ?: return null
    return "${dateTime.date.day}. ${toMonthAbbrev(lang)}"
}

/** "8 Oct" style day label for a Kotor calendar date, with localized month names. */
fun LocalDate.toDayLabel(lang: String): String {
    val months = monthAbbreviations[lang] ?: monthAbbreviations.getValue("en")
    return "$day ${months[month.number - 1]}"
}
