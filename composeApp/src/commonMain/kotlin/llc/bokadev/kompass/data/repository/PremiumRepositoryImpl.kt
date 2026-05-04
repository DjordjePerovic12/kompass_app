package llc.bokadev.kompass.data.repository

import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.repository.PremiumRepository

class PremiumRepositoryImpl(
    private val preferences: AppPreferences
) : PremiumRepository {

    override fun hasAccess(tier: String): Boolean {
        return getEntitlements().hasAccess(tier)
    }

    override fun getEntitlements(): PremiumEntitlements {
        return PremiumEntitlements(
            audioPass = preferences.hasAudioPass(),
            explorerPass = preferences.hasExplorerPass(),
            perksPass = preferences.hasPerksPass()
        )
    }

    override fun applyEntitlements(entitlements: PremiumEntitlements) {
        preferences.setAudioPass(entitlements.audioPass)
        preferences.setExplorerPass(entitlements.explorerPass)
        preferences.setPerksPass(entitlements.perksPass)
    }
}
