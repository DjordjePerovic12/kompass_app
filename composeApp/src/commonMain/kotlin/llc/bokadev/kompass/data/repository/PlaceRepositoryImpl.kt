package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.PlaceDto
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.Place
import llc.bokadev.kompass.domain.model.PlaceCategory
import llc.bokadev.kompass.domain.repository.PlaceRepository

class PlaceRepositoryImpl(
    private val supabase: SupabaseClient,
    private val appPreferences: AppPreferences
) : PlaceRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private fun Place.isMarkedMustSee(): Boolean {
        if (isMustSee) return true
        return tags.any { tag ->
            val normalized = tag.trim().lowercase().replace('-', '_').replace(' ', '_')
            normalized == "must_see"
        }
    }

    private fun cachePlaces(dtos: List<PlaceDto>) {
        appPreferences.setString(
            CACHE_KEY_ACTIVE_PLACES,
            json.encodeToString(ListSerializer(PlaceDto.serializer()), dtos)
        )
    }

    private fun readCachedPlaces(): List<PlaceDto> {
        val raw = appPreferences.getString(CACHE_KEY_ACTIVE_PLACES) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(PlaceDto.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    override suspend fun getActivePlaces(): Result<List<Place>> = runCatching {
        runCatching {
            supabase.from("places")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<PlaceDto>()
        }.fold(
            onSuccess = { remotePlaces ->
                cachePlaces(remotePlaces)
                remotePlaces
            },
            onFailure = { remoteError ->
                val cachedPlaces = readCachedPlaces()
                if (cachedPlaces.isNotEmpty()) {
                    cachedPlaces
                } else {
                    throw remoteError
                }
            }
        ).map { it.toDomain() }
    }

    override suspend fun getMustSeePlaces(): Result<List<Place>> = runCatching {
        getActivePlaces()
            .getOrThrow()
            .filter { it.isMarkedMustSee() }
            .sortedBy { it.sortOrder }
    }

    override suspend fun getPlacesByCategory(category: PlaceCategory): Result<List<Place>> = runCatching {
        getActivePlaces()
            .getOrThrow()
            .filter { it.category == category }
            .sortedBy { it.sortOrder }
    }

    override suspend fun getPlaceById(id: String): Result<Place> = runCatching {
        getActivePlaces()
            .getOrThrow()
            .firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("Place not found")
    }

    private companion object {
        const val CACHE_KEY_ACTIVE_PLACES = "offline_cache_active_places_v1"
    }
}
