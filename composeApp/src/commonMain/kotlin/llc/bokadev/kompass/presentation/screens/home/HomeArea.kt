package llc.bokadev.kompass.presentation.screens.home

import llc.bokadev.kompass.core.utils.AppStrings

enum class HomeArea(val preferenceValue: String) {
    DEVICE("device"), OLD_TOWN("old_town"), DOBROTA("dobrota"), PERAST("perast");

    fun matchesZone(zone: String?): Boolean {
        val normalized = zone?.trim()?.lowercase()?.replace(Regex("[\\s_-]+"), "_")
        return when (this) {
            DEVICE -> false
            OLD_TOWN -> normalized in setOf("old_town", "kotor", "kotor_old_town", "stari_grad")
            DOBROTA -> normalized == "dobrota"
            PERAST -> normalized == "perast"
        }
    }

    fun label(strings: AppStrings): String = when (this) {
        DEVICE -> strings.redesign.deviceLocation
        OLD_TOWN -> strings.redesign.oldTown
        DOBROTA -> "Dobrota"
        PERAST -> "Perast"
    }

    /** Just the area's own name (e.g. "Old Town", not "Kotor / Old Town") — for the Lamplight
     * top bar's large title, which already has its own "Around" label above it. */
    fun shortLabel(strings: AppStrings): String = when (this) {
        DEVICE -> strings.redesign.deviceLocation
        OLD_TOWN -> strings.redesign.oldTownShort
        DOBROTA -> "Dobrota"
        PERAST -> "Perast"
    }

    fun sectionTitle(strings: AppStrings): String = when (this) {
        DEVICE -> strings.nearbyYou
        OLD_TOWN -> strings.redesign.aroundOldTown
        DOBROTA -> strings.redesign.aroundDobrota
        PERAST -> strings.redesign.aroundPerast
    }

    companion object {
        const val PREFERENCE_KEY = "home_browsing_area"
        fun restore(value: String?): HomeArea =
            entries.firstOrNull { it.preferenceValue == value } ?: DEVICE
    }
}
