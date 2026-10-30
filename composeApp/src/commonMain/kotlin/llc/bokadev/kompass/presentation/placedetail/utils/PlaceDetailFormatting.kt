package llc.bokadev.kompass.presentation.placedetail.utils

import androidx.compose.runtime.Composable
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.uppercaseFor
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PriceIndicator
import llc.bokadev.kompass.presentation.shared.detail.normalizedForCompare
import llc.bokadev.kompass.presentation.shared.localizedTypeLabel

/** "CHURCH · OLD TOWN": one primary type plus the area, uppercased for the app language. */
@Composable
fun Place.detailTypeLabel(lang: String): String =
    listOfNotNull(localizedTypeLabel(), detailAreaLabel())
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
