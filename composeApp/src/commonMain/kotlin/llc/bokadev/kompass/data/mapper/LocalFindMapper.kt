package llc.bokadev.kompass.data.mapper

import llc.bokadev.kompass.data.remote.dto.LocalFindDto
import llc.bokadev.kompass.domain.model.LocalFind

fun LocalFindDto.toDomain(): LocalFind = LocalFind(
    id = id,
    cityId = cityId,
    title = title,
    body = body,
    audioFile = audioFile,
    deepBody = deepBody ?: emptyMap(),
    deepAudioFile = deepAudioFile,
    deepPhotos = deepPhotos.sanitizePhotoPaths(),
    deepAnchorMode = deepAnchorMode,
    deepLatitude = deepLatitude,
    deepLongitude = deepLongitude,
    location = location,
    latitude = latitude,
    longitude = longitude,
    photos = photos.sanitizePhotoPaths(),
    isActive = isActive,
    sortOrder = sortOrder
)
