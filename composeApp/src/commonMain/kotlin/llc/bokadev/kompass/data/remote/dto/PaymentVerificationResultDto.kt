package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import llc.bokadev.kompass.domain.model.PaymentVerificationResult
import llc.bokadev.kompass.domain.model.PaymentVerificationStatus

@Serializable
data class PaymentVerificationResultDto(
    val status: String,
    val message: String? = null,
    val entitlements: PremiumEntitlementsDto? = null
)

fun PaymentVerificationResultDto.toDomain(): PaymentVerificationResult = PaymentVerificationResult(
    status = when (status.uppercase()) {
        "SUCCEEDED", "SUCCESS", "FINISHED" -> PaymentVerificationStatus.SUCCEEDED
        "PENDING" -> PaymentVerificationStatus.PENDING
        "CANCELED", "CANCELLED" -> PaymentVerificationStatus.CANCELED
        else -> PaymentVerificationStatus.FAILED
    },
    entitlements = entitlements?.toDomain() ?: llc.bokadev.kompass.domain.model.PremiumEntitlements(),
    message = message
)
