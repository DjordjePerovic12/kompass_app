package llc.bokadev.kompass.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.data.remote.dto.CreateCheckoutSessionRequest
import llc.bokadev.kompass.data.remote.dto.PaymentCheckoutSessionDto
import llc.bokadev.kompass.data.remote.dto.PaymentVerificationResultDto
import llc.bokadev.kompass.data.remote.dto.toDomain
import llc.bokadev.kompass.domain.model.PaymentCheckoutSession
import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.model.PremiumProduct
import llc.bokadev.kompass.domain.repository.PaymentRepository

class PaymentRepositoryImpl(
    private val client: HttpClient
) : PaymentRepository {

    override suspend fun startCheckout(
        product: PremiumProduct,
        locale: String,
        platform: String
    ): Result<PaymentCheckoutSession> = runCatching {
        val baseUrl = BuildKonfig.PAYMENT_BACKEND_BASE_URL.trimEnd('/')
        require(baseUrl.isNotBlank()) {
            "Payment backend is not configured yet. Set PAYMENT_BACKEND_BASE_URL in local.properties."
        }

        client.post("$baseUrl/payments/checkout-sessions") {
            accept(ContentType.Application.Json)
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(
                CreateCheckoutSessionRequest(
                    productId = product.id,
                    locale = locale,
                    platform = platform
                )
            )
        }.body<PaymentCheckoutSessionDto>().toDomain()
    }

    override suspend fun verifyCheckout(sessionId: String): Result<PaymentVerificationResult> = runCatching {
        val baseUrl = BuildKonfig.PAYMENT_BACKEND_BASE_URL.trimEnd('/')
        require(baseUrl.isNotBlank()) {
            "Payment backend is not configured yet. Set PAYMENT_BACKEND_BASE_URL in local.properties."
        }

        client.post("$baseUrl/payments/checkout-sessions/$sessionId/verify") {
            accept(ContentType.Application.Json)
            headers {
                append(HttpHeaders.CacheControl, "no-cache")
            }
        }.body<PaymentVerificationResultDto>().toDomain()
    }
}
