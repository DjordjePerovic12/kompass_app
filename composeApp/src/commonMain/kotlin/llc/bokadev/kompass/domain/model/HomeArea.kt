package llc.bokadev.kompass.domain.model

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

    companion object {
        const val PREFERENCE_KEY = "home_browsing_area"
        fun restore(value: String?): HomeArea =
            entries.firstOrNull { it.preferenceValue == value } ?: DEVICE
    }
}
