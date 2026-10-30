package llc.bokadev.kompass.presentation.screens.placedetail

import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.DetailStrings
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.BestTime
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PriceIndicator

fun String.prettyLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

fun Int.formatDuration(): String = when {
    this < 60 -> "${this} min"
    this % 60 == 0 -> "${this / 60} h"
    else -> "${this / 60} h ${this % 60} min"
}

/** "CHURCH · OLD TOWN": one primary type plus the area, uppercased for the app language. */
fun Place.detailTypeLabel(strings: AppStrings, lang: String): String =
    listOfNotNull(localizedTypeLabel(strings), detailAreaLabel())
        .joinToString(" · ")
        .uppercaseFor(lang)

/** The place's area as a plain label ("Old Town"), or null when the CMS has none. */
fun Place.detailAreaLabel(): String? = zone?.takeIf { it.isNotBlank() }?.toTitleCaseLabel()

/** Opening hours exactly as entered in the CMS (localized when it is, else the first value). */
fun Place.detailOpeningHours(lang: String): String? {
    val hours = openingHours ?: return null
    return (hours[lang] ?: hours["en"] ?: hours.values.firstOrNull { it.isNotBlank() })
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

fun Int.toVisitLength(strings: DetailStrings): String? = when {
    this <= 0 -> null
    this < 60 -> strings.aboutMinutes.replace("{n}", toString())
    this % 60 == 0 -> strings.aboutHours.replace("{h}", (this / 60).toString())
    else -> strings.aboutHoursMinutes.replace("{h}", (this / 60).toString()).replace("{m}", (this % 60).toString())
}

/** Null for "any time": a generic default isn't advice, so the row hides (spec §1.4). */
fun BestTime.toDetailLabel(strings: DetailStrings): String? = when (this) {
    BestTime.MORNING -> strings.bestMorning
    BestTime.AFTERNOON -> strings.bestAfternoon
    BestTime.EVENING -> strings.bestEvening
    BestTime.ANYTIME -> null
}

fun PriceIndicator.toEuros(): String = when (this) {
    PriceIndicator.BUDGET -> "€"
    PriceIndicator.MODERATE -> "€€"
    PriceIndicator.EXPENSIVE -> "€€€"
}

/**
 * The local tip, unless it is blank or just repeats the descriptions (compared lowercase, with
 * punctuation stripped and whitespace collapsed), so the yellow panel always adds something.
 */
fun Place.detailLocalTip(lang: String): String? {
    val tip = localizedLocalsTip(lang).trim().takeIf { it.isNotBlank() } ?: return null
    val normalizedTip = tip.normalizedForCompare()
    if (normalizedTip.isBlank()) return null
    val descriptions = (localizedDescription(lang) + " " + localizedLongDescription(lang)).normalizedForCompare()
    return tip.takeUnless { normalizedTip in descriptions }
}

/**
 * The About text: the long description, unless it is blank or merely repeats the short one shown
 * under the title (the CMS often has the same sentence in both fields).
 */
fun Place.detailAboutText(lang: String): String? {
    val long = localizedLongDescription(lang).trim().takeIf { it.isNotBlank() } ?: return null
    val normalizedLong = long.normalizedForCompare()
    return long.takeUnless { normalizedLong in localizedDescription(lang).normalizedForCompare() }
}

internal fun String.normalizedForCompare(): String =
    lowercase()
        .filter { it.isLetterOrDigit() || it.isWhitespace() }
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ")
