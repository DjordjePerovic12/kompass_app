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
            title = "Audio Pass",
            priceLabel = "€4.99",
            description = "Unlock audio stories for landmarks, fortresses, trails, and activities with learn-more moments you can listen to while exploring.",
            features = listOf(
                "Activity and place audio guides",
                "Hands-free storytelling while exploring",
                "Perfect for solo walkers and self-guided discovery"
            )
        ),
        PremiumProduct(
            id = "explorer-pass-kotor",
            tier = "explorer_pass",
            title = "Explorer Pass",
            priceLabel = "€9.99",
            description = "Includes everything in Audio Pass plus deeper editorial guides and itinerary logic for people who want more context and better trip structure.",
            features = listOf(
                "Everything in Audio Pass",
                "Premium deep-dive activity descriptions",
                "Custom itinerary foundation"
            )
        )
    )

    fun find(productId: String): PremiumProduct? = products.firstOrNull { it.id == productId }
}
