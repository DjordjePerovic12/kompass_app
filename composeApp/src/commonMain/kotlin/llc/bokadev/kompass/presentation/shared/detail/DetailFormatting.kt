package llc.bokadev.kompass.presentation.shared.detail

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

internal fun String.normalizedForCompare(): String =
    lowercase()
        .filter { it.isLetterOrDigit() || it.isWhitespace() }
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ")
