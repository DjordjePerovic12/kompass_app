package llc.bokadev.kompass.presentation.shared.detail

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.detail_about_hours
import kompass.composeapp.generated.resources.detail_about_hours_minutes
import kompass.composeapp.generated.resources.detail_about_minutes
import kompass.composeapp.generated.resources.detail_best_afternoon
import kompass.composeapp.generated.resources.detail_best_evening
import kompass.composeapp.generated.resources.detail_best_morning
import llc.bokadev.kompass.domain.model.BestTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun String.prettyLabel(): String =
    split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.lowercase().replaceFirstChar { first ->
                if (first.isLowerCase()) first.titlecase() else first.toString()
            }
        }

@Composable
fun Int.toVisitLength(): String? = when {
    this <= 0 -> null
    this < 60 -> stringResource(Res.string.detail_about_minutes, toString())
    this % 60 == 0 -> stringResource(Res.string.detail_about_hours, (this / 60).toString())
    else -> stringResource(Res.string.detail_about_hours_minutes, (this / 60).toString(), (this % 60).toString())
}

/** Null for "any time": a generic default isn't advice, so the row hides (spec §1.4). */
fun BestTime.detailLabelRes(): StringResource? = when (this) {
    BestTime.MORNING -> Res.string.detail_best_morning
    BestTime.AFTERNOON -> Res.string.detail_best_afternoon
    BestTime.EVENING -> Res.string.detail_best_evening
    BestTime.ANYTIME -> null
}

internal fun String.normalizedForCompare(): String =
    lowercase()
        .filter { it.isLetterOrDigit() || it.isWhitespace() }
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ")
