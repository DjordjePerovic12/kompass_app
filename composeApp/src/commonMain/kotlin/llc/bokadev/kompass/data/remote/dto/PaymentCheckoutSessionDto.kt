package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import llc.bokadev.kompass.domain.model.PaymentCheckoutSession

@Serializable
data class PaymentCheckoutSessionDto(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("checkout_url")
    val checkoutUrl: String,
    @SerialName("success_url")
    val successUrl: String,
    @SerialName("cancel_url")
    val cancelUrl: String,
    @SerialName("error_url")
    val errorUrl: String,
    @SerialName("expires_at")
    val expiresAt: String? = null
)

fun PaymentCheckoutSessionDto.toDomain(): PaymentCheckoutSession = PaymentCheckoutSession(
    sessionId = sessionId,
    productId = productId,
    checkoutUrl = checkoutUrl,
    successUrl = successUrl,
    cancelUrl = cancelUrl,
    errorUrl = errorUrl,
    expiresAt = expiresAt
)
