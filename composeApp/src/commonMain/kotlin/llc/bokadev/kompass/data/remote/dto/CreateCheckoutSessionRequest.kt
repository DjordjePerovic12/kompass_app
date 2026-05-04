package llc.bokadev.kompass.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCheckoutSessionRequest(
    @SerialName("product_id")
    val productId: String,
    val locale: String,
    val platform: String
)
