package llc.bokadev.kompass.presentation.shared

import androidx.compose.runtime.Composable
import kompass.composeapp.generated.resources.Res
import kompass.composeapp.generated.resources.redesign_notice_general
import kompass.composeapp.generated.resources.redesign_notice_important
import kompass.composeapp.generated.resources.redesign_notice_until
import kompass.composeapp.generated.resources.redesign_notice_urgent
import llc.bokadev.kompass.core.utils.toNoticeDate
import llc.bokadev.kompass.domain.model.InfoNotice
import org.jetbrains.compose.resources.stringResource

fun InfoNotice.toNoticeLevel(): NoticeLevel = when (priorityRank()) {
    0 -> NoticeLevel.URGENT
    1 -> NoticeLevel.IMPORTANT
    else -> NoticeLevel.GENERAL
}

/** "Important · until 12. Oct" (or just "Important" with no end date) — never fabricated, hides the "until" segment when `endsAt` is absent. */
@Composable
fun InfoNotice.toNoticeLabel(lang: String): String {
    val levelText = when (toNoticeLevel()) {
        NoticeLevel.URGENT -> stringResource(Res.string.redesign_notice_urgent)
        NoticeLevel.IMPORTANT -> stringResource(Res.string.redesign_notice_important)
        NoticeLevel.GENERAL -> stringResource(Res.string.redesign_notice_general)
    }
    val untilDate = endsAt?.toNoticeDate(lang)
    val until = stringResource(Res.string.redesign_notice_until)
    return if (untilDate != null) "$levelText · $until $untilDate" else levelText
}
