package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.PremiumEntitlements

interface PremiumRepository {
    fun hasAccess(tier: String): Boolean
    fun getEntitlements(): PremiumEntitlements
    fun applyEntitlements(entitlements: PremiumEntitlements)
}
