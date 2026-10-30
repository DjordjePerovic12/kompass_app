package llc.bokadev.kompass.presentation.shared

import llc.bokadev.kompass.domain.model.HomeArea

/** "In the bay": within this distance of Kotor Old Town, distances are shown; outside, never. */
internal const val REGION_RADIUS_KM = 30.0

/** The old distance-based bucket; only found in snapshots saved before the pilot. */
private const val LEGACY_GROUP_WORTH_THE_DRIVE = "worth_the_drive"
const val GROUP_OTHER = "other"

/**
 * The geographic area an item is grouped under: a curated Home area, else its own normalized area
 * ("Kotor Bay" and "kotor_bay" are one group), else Other. Never distance-based: Worth the Drive is an
 * editorial collection (`worth_the_drive_items`), not a geographic bucket. Idempotent, so stored keys
 * can be passed back through it; the pre-pilot "worth_the_drive" bucket becomes Other.
 */
internal fun areaKeyFor(area: String?): String {
    HomeArea.entries.filter { it != HomeArea.DEVICE }.firstOrNull { it.matchesZone(area) }?.let { return it.preferenceValue }
    val normalized = area?.lowercase()?.toAreaSlug()
    return normalized?.takeIf { it.isNotEmpty() && it != LEGACY_GROUP_WORTH_THE_DRIVE } ?: GROUP_OTHER
}

/**
 * Runs of anything but letters and digits become one "_", with none at either end
 * ("  Kotor-Bay " → "kotor_bay"). Hand-rolled because `\p{L}` regex classes aren't supported on Kotlin/Native.
 */
private fun String.toAreaSlug(): String = buildString {
    var separatorPending = false
    for (char in this@toAreaSlug) {
        if (char.isLetterOrDigit()) {
            if (separatorPending && isNotEmpty()) append('_')
            separatorPending = false
            append(char)
        } else {
            separatorPending = true
        }
    }
}
