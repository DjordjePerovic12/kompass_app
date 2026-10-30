package llc.bokadev.kompass.presentation.shared

import llc.bokadev.kompass.core.utils.EssentialsStrings
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.UtilityCategory

/** A readable title for a bare CMS link: its host without "www.", e.g. "redtaxikotor.com". */
fun String.linkTitle(): String =
    substringAfter("://").substringBefore("/").removePrefix("www.").ifBlank { this }

/** "86 m", "130 m" (10 m steps above 100 m), "1.2 km". */
fun Int.formatDistance(): String = when {
    this < 100 -> "$this m"
    this < 1000 -> "${(this + 5) / 10 * 10} m"
    else -> {
        val tenths = (this + 50) / 100
        "${tenths / 10}.${tenths % 10} km"
    }
}
