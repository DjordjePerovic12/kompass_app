package llc.bokadev.kompass.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.map
import llc.bokadev.kompass.core.utils.networking.retrieveAPIResponse
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
    ): Resource<PaymentCheckoutSession, NetworkError, String?> {
        val baseUrl = BuildKonfig.PAYMENT_BACKEND_BASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) {
            return Resource.Error(
                NetworkError.UNKNOWN,
                "Payment backend is not configured yet. Set PAYMENT_BACKEND_BASE_URL in local.properties."
            )
        }

        return retrieveAPIResponse<PaymentCheckoutSessionDto> {
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
            }
        }.map { it.toDomain() }
    }

    override suspend fun verifyCheckout(sessionId: String): Resource<PaymentVerificationResult, NetworkError, String?> {
        val baseUrl = BuildKonfig.PAYMENT_BACKEND_BASE_URL.trimEnd('/')
        if (baseUrl.isBlank()) {
            return Resource.Error(
                NetworkError.UNKNOWN,
                "Payment backend is not configured yet. Set PAYMENT_BACKEND_BASE_URL in local.properties."
            )
        }

        return retrieveAPIResponse<PaymentVerificationResultDto> {
            client.post("$baseUrl/payments/checkout-sessions/$sessionId/verify") {
                accept(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.CacheControl, "no-cache")
                }
            }
        }.map { it.toDomain() }
    }
}
