package llc.bokadev.kompass.domain.usecase

import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.repository.PaymentRepository

class StartPremiumCheckoutUseCase(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(
        product: PremiumProduct,
        locale: String,
        platform: String
    ): Result<PaymentCheckoutSession> = paymentRepository.startCheckout(product, locale, platform)
}
