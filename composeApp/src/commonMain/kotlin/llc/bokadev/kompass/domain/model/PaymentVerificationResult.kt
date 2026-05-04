package llc.bokadev.kompass.domain.model

enum class PaymentVerificationStatus {
    SUCCEEDED,
    PENDING,
    FAILED,
    CANCELED
}

data class PaymentVerificationResult(
    val status: PaymentVerificationStatus,
    val entitlements: PremiumEntitlements = PremiumEntitlements(),
    val message: String? = null
)
