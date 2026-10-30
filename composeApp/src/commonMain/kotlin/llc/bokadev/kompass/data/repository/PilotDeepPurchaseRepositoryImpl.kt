package llc.bokadev.kompass.data.repository

import llc.bokadev.kompass.domain.model.DeepStoreProduct
import llc.bokadev.kompass.domain.model.PremiumEntitlements
import llc.bokadev.kompass.domain.repository.DeepPurchaseRepository
import llc.bokadev.kompass.domain.repository.PremiumRepository

class PilotDeepPurchaseRepositoryImpl(
    private val premiumRepository: PremiumRepository
) : DeepPurchaseRepository {

    override suspend fun syncEntitlements(): PremiumEntitlements {
        val cleared = PremiumEntitlements()
        premiumRepository.applyEntitlements(cleared)
        return cleared
    }

    override suspend fun getDeepProduct(): Result<DeepStoreProduct> {
        return Result.failure(IllegalStateException("Deep is disabled in the pilot build."))
    }

    override suspend fun purchaseDeep(): Result<PremiumEntitlements> {
        return Result.failure(IllegalStateException("Deep is disabled in the pilot build."))
    }

    override suspend fun restoreDeep(): Result<PremiumEntitlements> {
        return Result.success(syncEntitlements())
    }
}
