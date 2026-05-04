package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.repository.PremiumRepository

class HasPremiumAccessUseCase(
    private val repository: PremiumRepository
) {
    operator fun invoke(tier: String): Boolean = repository.hasAccess(tier)
}
