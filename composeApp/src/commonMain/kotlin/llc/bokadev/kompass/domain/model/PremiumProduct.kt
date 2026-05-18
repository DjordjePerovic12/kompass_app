package llc.bokadev.kompass.domain.model

data class PremiumProduct(
    val id: String,
    val tier: String,
    val title: String,
    val priceLabel: String,
    val description: String,
    val features: List<String>
)

object PremiumCatalog {
    val products: List<PremiumProduct> = listOf(
        PremiumProduct(
            id = "audio-pass-kotor",
            tier = "audio_pass",
            title = "KOMPASS Deep",
            priceLabel = "€4.99",
            description = "A quieter companion layer across selected places and experiences in Kotor, adding short contextual moments, atmosphere, and deeper awareness while you explore.",
            features = listOf(
                "Deep-supported place and activity experiences",
                "Subtle audio companionship during walks",
                "Layered local, historical, and spatial context",
                "One-time access across all Deep-supported experiences in Kotor"
            )
        )
    )

    fun find(productId: String): PremiumProduct? = products.firstOrNull { it.id == productId }
}
