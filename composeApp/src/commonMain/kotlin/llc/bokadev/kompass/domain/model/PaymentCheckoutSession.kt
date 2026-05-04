package llc.bokadev.kompass.domain.model

data class PaymentCheckoutSession(
    val sessionId: String,
    val productId: String,
    val checkoutUrl: String,
    val successUrl: String,
    val cancelUrl: String,
    val errorUrl: String,
    val expiresAt: String? = null
)
