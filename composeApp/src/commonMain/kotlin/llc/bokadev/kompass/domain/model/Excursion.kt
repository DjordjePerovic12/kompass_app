package llc.bokadev.kompass.domain.model

/** Editorial membership is independent of geography and of place/activity relationships. */
data class Excursion(
    val id: String,
    val place: Place? = null,
    val activity: Experience? = null,
    val interests: Set<String> = emptySet(),
    val timeBand: String? = null,
    val planningLevel: String? = null,
    val sortOrder: Int = 0
) {
    init { require((place != null) xor (activity != null)) }
    val photo: String? get() = place?.photos?.firstOrNull() ?: activity?.photos?.firstOrNull()
}

data class ExcursionFilter(
    val interests: Set<String> = emptySet(),
    val timeBand: String? = null,
    val planningLevel: String? = null
) {
    fun matches(item: Excursion): Boolean =
        (interests.isEmpty() || interests.any { it in item.interests }) &&
            (timeBand == null || item.timeBand == timeBand) &&
            (planningLevel == null || item.planningLevel == planningLevel)
}

data class ExcursionResult<T>(val items: List<T>, val isCached: Boolean = false)
