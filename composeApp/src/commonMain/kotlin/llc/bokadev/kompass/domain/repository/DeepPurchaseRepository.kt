package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.DeepStoreProduct
import llc.bokadev.kompass.domain.model.PremiumEntitlements

interface DeepPurchaseRepository {
    suspend fun syncEntitlements(): PremiumEntitlements
    suspend fun getDeepProduct(): Result<DeepStoreProduct>
    suspend fun purchaseDeep(): Result<PremiumEntitlements>
    suspend fun restoreDeep(): Result<PremiumEntitlements>
}
