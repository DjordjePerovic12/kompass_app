package llc.bokadev.kompass.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.mapCatching
import llc.bokadev.kompass.core.utils.networking.recover
import llc.bokadev.kompass.core.utils.networking.safeSupabaseCall
import llc.bokadev.kompass.data.mapper.toDomain
import llc.bokadev.kompass.data.remote.dto.PlaceDto
import llc.bokadev.kompass.core.utils.AppPreferences
import llc.bokadev.kompass.domain.model.Place
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

    override suspend fun getActivePlaces(): Resource<List<Place>, NetworkError, String?> =
        safeSupabaseCall {
            supabase.from("places")
                .select {
                    filter {
                        eq("is_active", true)
                    }
                    order(column = "sort_order", order = Order.ASCENDING)
                }
                .decodeList<PlaceDto>()
                .also { remotePlaces -> cachePlaces(remotePlaces) }
        }.recover { _, _ ->
            readCachedPlaces().ifEmpty { null }
        }.mapCatching { places -> places.map { it.toDomain() } }

    override suspend fun getMustSeePlaces(): Resource<List<Place>, NetworkError, String?> =
        getActivePlaces().mapCatching { places ->
            places
                .filter { it.isMarkedMustSee() }
                .sortedBy { it.sortOrder }
        }

    override suspend fun getPlaceById(id: String): Resource<Place, NetworkError, String?> =
        when (val places = getActivePlaces()) {
            is Resource.Error -> places
            is Resource.Success -> places.data.firstOrNull { it.id == id }?.let { Resource.Success(it) }
                ?: Resource.Error(NetworkError.NOT_FOUND, "Place not found")
        }

    private companion object {
        const val CACHE_KEY_ACTIVE_PLACES = "offline_cache_active_places_v1"
    }
}
