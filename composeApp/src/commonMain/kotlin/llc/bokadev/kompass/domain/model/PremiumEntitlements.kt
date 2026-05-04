package llc.bokadev.kompass.domain.model

data class PremiumEntitlements(
    val audioPass: Boolean = false,
    val explorerPass: Boolean = false,
    val perksPass: Boolean = false
) {
    fun hasAccess(tier: String): Boolean {
        return when (tier.lowercase()) {
            "free" -> true
            "audio_pass" -> audioPass || explorerPass || perksPass
            "explorer_pass" -> explorerPass || perksPass
            "perks_pass" -> perksPass
            else -> false
        }
    }
}
