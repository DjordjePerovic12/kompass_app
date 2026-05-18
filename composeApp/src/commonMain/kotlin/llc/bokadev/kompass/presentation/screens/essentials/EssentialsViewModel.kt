package llc.bokadev.kompass.presentation.screens.essentials

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.presentation.base.BaseEvent
import llc.bokadev.kompass.core.presentation.base.BaseState
import llc.bokadev.kompass.core.presentation.base.BaseViewModel
import llc.bokadev.kompass.domain.location.UserLocationProvider
import llc.bokadev.kompass.domain.model.CityEssential
import llc.bokadev.kompass.domain.model.EssentialCategory
import llc.bokadev.kompass.domain.model.GeoPoint
import llc.bokadev.kompass.domain.model.Utility
import llc.bokadev.kompass.domain.model.UtilityCategory
import llc.bokadev.kompass.domain.usecase.GetEssentialsUseCase
import llc.bokadev.kompass.domain.usecase.GetUtilitiesUseCase
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class UtilityCategorySection(
    val category: UtilityCategory,
    val totalCount: Int,
    val nearbyCount: Int,
    val closestDistanceMeters: Int?,
    val previewUtilities: List<Utility>,
    val mapCenter: GeoPoint,
    val showCurrentLocation: Boolean
)

data class EssentialsState(
    override val isLoading: Boolean = true,
    override val error: String? = null,
    val selectedTab: EssentialsTab = EssentialsTab.INFO,
    val groupedEssentials: Map<EssentialCategory, List<CityEssential>> = emptyMap(),
    val expandedIds: Set<String> = emptySet(),
    val sections: List<UtilityCategorySection> = emptyList(),
    val currentLocation: GeoPoint? = null
) : BaseState()

enum class EssentialsTab {
    INFO,
    UTILITY
}

sealed interface EssentialsEvent : BaseEvent {
    data class SelectTab(val tab: EssentialsTab) : EssentialsEvent
    data class ToggleItem(val id: String) : EssentialsEvent
    data object Retry : EssentialsEvent
}

class EssentialsViewModel(
    private val getEssentials: GetEssentialsUseCase,
    private val getUtilities: GetUtilitiesUseCase,
    private val userLocationProvider: UserLocationProvider
) : BaseViewModel<EssentialsState, EssentialsEvent>() {

    override val initialState = EssentialsState()

    init {
        load()
    }

    override fun onIntent(event: EssentialsEvent) {
        when (event) {
            is EssentialsEvent.SelectTab -> _state.update { it.copy(selectedTab = event.tab) }
            is EssentialsEvent.ToggleItem -> toggleItem(event.id)
            EssentialsEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentLocation = runCatching { userLocationProvider.getCurrentLocation() }.getOrNull()
            val origin = currentLocation ?: KOTOR_OLD_TOWN_CENTER

            val essentialsResult = getEssentials()
            val utilitiesResult = getUtilities()

            val groupedEssentials = essentialsResult.getOrDefault(emptyList()).groupBy { it.category }
            val sections = utilitiesResult.getOrDefault(emptyList())
                .filter { it.latitude != null && it.longitude != null }
                .groupBy { it.category }
                .mapNotNull { (category, items) ->
                    buildSection(category, items, origin, currentLocation)
                }
                .sortedBy { it.category.orderIndex() }

            val error = utilitiesResult.exceptionOrNull()?.message
                ?: essentialsResult.exceptionOrNull()?.message

            _state.update {
                it.copy(
                    isLoading = false,
                    error = if (groupedEssentials.isEmpty() && sections.isEmpty()) error else null,
                    groupedEssentials = groupedEssentials,
                    sections = sections,
                    currentLocation = currentLocation
                )
            }
        }
    }

    private fun buildSection(
        category: UtilityCategory,
        items: List<Utility>,
        origin: GeoPoint,
        currentLocation: GeoPoint?
    ): UtilityCategorySection? {
        val ranked = items
            .mapNotNull { utility ->
                val lat = utility.latitude ?: return@mapNotNull null
                val lng = utility.longitude ?: return@mapNotNull null
                RankedUtility(
                    utility = utility,
                    point = GeoPoint(lat, lng),
                    distanceKm = haversineDistanceKm(origin, GeoPoint(lat, lng))
                )
            }
            .sortedBy { it.distanceKm }

        if (ranked.isEmpty()) return null

        val nearby = ranked.filter { it.distanceKm <= NEARBY_RADIUS_KM }
        val preview = if (nearby.isNotEmpty()) {
            nearby.take(MAX_PREVIEW_PINS)
        } else {
            val anchor = ranked.first()
            ranked
                .filter { candidate ->
                    haversineDistanceKm(anchor.point, candidate.point) <= FALLBACK_CLUSTER_RADIUS_KM
                }
                .take(FALLBACK_PREVIEW_PINS)
                .ifEmpty { listOf(anchor) }
        }
        val previewPoints = preview.map { it.point }
        val center = GeoPoint(
            latitude = previewPoints.map { it.latitude }.average(),
            longitude = previewPoints.map { it.longitude }.average()
        )
        val showCurrentLocation = currentLocation?.let { user ->
            preview.minOfOrNull { it.distanceKm }?.let { closest ->
                closest <= CURRENT_LOCATION_PREVIEW_LIMIT_KM
            } ?: false
        } ?: false

        return UtilityCategorySection(
            category = category,
            totalCount = ranked.size,
            nearbyCount = nearby.size,
            closestDistanceMeters = (ranked.firstOrNull()?.distanceKm?.times(1000))?.toInt(),
            previewUtilities = preview.map { it.utility },
            mapCenter = center,
            showCurrentLocation = showCurrentLocation
        )
    }

    private fun toggleItem(id: String) {
        _state.update {
            val newExpanded = if (id in it.expandedIds) it.expandedIds - id else it.expandedIds + id
            it.copy(expandedIds = newExpanded)
        }
    }

    private fun haversineDistanceKm(from: GeoPoint, to: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val dLat = (to.latitude - from.latitude).toRadians()
        val dLon = (to.longitude - from.longitude).toRadians()
        val fromLat = from.latitude.toRadians()
        val toLat = to.latitude.toRadians()

        val a = sin(dLat / 2).pow(2) +
            cos(fromLat) * cos(toLat) * sin(dLon / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadiusKm * c
    }

    private fun Double.toRadians(): Double = this * (kotlin.math.PI / 180.0)

    private data class RankedUtility(
        val utility: Utility,
        val point: GeoPoint,
        val distanceKm: Double
    )

    private companion object {
        const val NEARBY_RADIUS_KM = 1.2
        const val MAX_PREVIEW_PINS = 5
        const val CURRENT_LOCATION_PREVIEW_LIMIT_KM = 2.8
        const val FALLBACK_CLUSTER_RADIUS_KM = 1.6
        const val FALLBACK_PREVIEW_PINS = 3

        val KOTOR_OLD_TOWN_CENTER = GeoPoint(
            latitude = 42.4246,
            longitude = 18.7712
        )
    }
}

private fun UtilityCategory.orderIndex(): Int = when (this) {
    UtilityCategory.ATM -> 0
    UtilityCategory.PHARMACY -> 1
    UtilityCategory.SUPERMARKET -> 2
    UtilityCategory.SHOP -> 3
    UtilityCategory.PARKING -> 4
    UtilityCategory.GAS_STATION -> 5
}
