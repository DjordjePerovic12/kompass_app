package llc.bokadev.kompass.domain.model

data class Utility(
    val id: String,
    val name: String,
    val category: UtilityCategory,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val googlePlaceId: String?,
    val sortOrder: Int = 0
)

enum class UtilityCategory {
    ATM,
    PHARMACY,
    SUPERMARKET,
    SHOP,
    PARKING,
    GAS_STATION,
    EMERGENCY
}
