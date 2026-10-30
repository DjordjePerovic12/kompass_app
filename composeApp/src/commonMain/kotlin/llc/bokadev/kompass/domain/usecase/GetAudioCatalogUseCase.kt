package llc.bokadev.kompass.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.core.utils.networking.getOrNull
import llc.bokadev.kompass.core.utils.networking.map
import llc.bokadev.kompass.domain.model.AudioCatalog
import llc.bokadev.kompass.domain.model.AudioCatalogEntry
import llc.bokadev.kompass.domain.model.AudioSource
import llc.bokadev.kompass.domain.repository.PlaceRepository

/**
 * The free audio catalog: active places, activities and stories with a regular audio file in at
 * least one language. Sources load independently; the catalog fails only if all of them do.
 */
class GetAudioCatalogUseCase(
    private val placeRepository: PlaceRepository,
    private val getActivities: GetActivitiesUseCase,
    private val getLocalFinds: GetLocalFindsUseCase
) {
    suspend operator fun invoke(): Resource<AudioCatalog, NetworkError, String?> = coroutineScope {
        val places = async {
            placeRepository.getActivePlaces().map { list ->
                list.map { p ->
                    AudioCatalogEntry(
                        AudioSource.PLACE, p.id, p.name, area = null, zone = p.zone, latitude = p.latitude, longitude = p.longitude,
                        files = p.audioFile.orEmpty(), photo = p.photos.firstOrNull(), sortOrder = p.sortOrder,
                        subtype = p.subCategory?.takeIf { it.isNotBlank() && !p.isDestinationOverview }
                    )
                }
            }
        }
        val activities = async {
            getActivities().map { list ->
                list.filter { it.isActive }.map { a ->
                    AudioCatalogEntry(
                        AudioSource.ACTIVITY, a.id, a.name, area = a.location, zone = null, latitude = a.latitude, longitude = a.longitude,
                        files = a.audioFile.orEmpty(), photo = a.photos.firstOrNull(), sortOrder = a.sortOrder
                    )
                }
            }
        }
        val stories = async {
            getLocalFinds().map { list ->
                list.filter { it.isActive }.map { s ->
                    AudioCatalogEntry(
                        AudioSource.STORY, s.id, s.title, area = s.location, zone = null, latitude = s.latitude, longitude = s.longitude,
                        files = s.audioFile.orEmpty(), photo = s.photos.firstOrNull(), sortOrder = s.sortOrder
                    )
                }
            }
        }
        val results = mapOf(AudioSource.PLACE to places.await(), AudioSource.ACTIVITY to activities.await(), AudioSource.STORY to stories.await())
        val failed = results.filterValues { it is Resource.Error }.keys
        if (failed.size == results.size) {
            results.values.first() as? Resource.Error<NetworkError, String?> ?: Resource.Error(NetworkError.UNKNOWN, "No content")
        } else {
            val entries = results.values.mapNotNull { it.getOrNull() }.flatten().filter { it.languages.isNotEmpty() }
            Resource.Success(AudioCatalog(entries, failed))
        }
    }
}
