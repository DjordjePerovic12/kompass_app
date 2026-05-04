package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.model.PremiumProduct

interface PaymentRepository {
    suspend fun startCheckout(
        product: PremiumProduct,
        locale: String,
        platform: String
    ): Result<PaymentCheckoutSession>

    suspend fun verifyCheckout(sessionId: String): Result<PaymentVerificationResult>
}
