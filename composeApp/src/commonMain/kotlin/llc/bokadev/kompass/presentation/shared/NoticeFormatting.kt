package llc.bokadev.kompass.presentation.shared

import llc.bokadev.kompass.core.utils.AppStrings
import llc.bokadev.kompass.core.utils.localizedTypeLabel
import llc.bokadev.kompass.core.utils.toTitleCaseLabel
import llc.bokadev.kompass.core.utils.toClockTime
import llc.bokadev.kompass.core.utils.toNoticeDate
import llc.bokadev.kompass.domain.model.Event
import llc.bokadev.kompass.domain.model.InfoNotice
import llc.bokadev.kompass.domain.model.NearbyPlace
import llc.bokadev.kompass.domain.model.Place
import kotlin.math.roundToInt

fun InfoNotice.toNoticeLevel(): NoticeLevel = when (priorityRank()) {
    0 -> NoticeLevel.URGENT
    1 -> NoticeLevel.IMPORTANT
    else -> NoticeLevel.GENERAL
}

/** "Important · until 12. Oct" (or just "Important" with no end date) — never fabricated, hides the "until" segment when `endsAt` is absent. */
fun InfoNotice.toNoticeLabel(lang: String, strings: AppStrings): String {
    val levelText = when (toNoticeLevel()) {
        NoticeLevel.URGENT -> strings.redesign.noticeUrgent
        NoticeLevel.IMPORTANT -> strings.redesign.noticeImportant
        NoticeLevel.GENERAL -> strings.redesign.noticeGeneral
    }
    val untilDate = endsAt?.toNoticeDate(lang)
    return if (untilDate != null) "$levelText · ${strings.redesign.noticeUntil} $untilDate" else levelText
}
