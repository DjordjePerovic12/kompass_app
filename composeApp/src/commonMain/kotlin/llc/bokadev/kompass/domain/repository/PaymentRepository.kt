package llc.bokadev.kompass.domain.repository

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.model.PremiumProduct

interface PaymentRepository {
    suspend fun startCheckout(
        product: PremiumProduct,
        locale: String,
        platform: String
    ): Resource<PaymentCheckoutSession, NetworkError, String?>

    suspend fun verifyCheckout(sessionId: String): Resource<PaymentVerificationResult, NetworkError, String?>
}
