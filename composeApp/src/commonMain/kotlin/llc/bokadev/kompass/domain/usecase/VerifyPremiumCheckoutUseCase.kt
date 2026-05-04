package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.repository.PaymentRepository

class VerifyPremiumCheckoutUseCase(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(sessionId: String): Result<PaymentVerificationResult> =
        paymentRepository.verifyCheckout(sessionId)
}
