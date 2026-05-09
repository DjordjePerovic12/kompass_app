package llc.bokadev.kompass.domain.model

fun Place.isMarkedMustSee(): Boolean {
    if (isMustSee) return true
    return tags.any { tag ->
        val normalized = tag.trim().lowercase().replace('-', '_').replace(' ', '_')
        normalized == "must_see"
    }
}

fun List<Place>.favoritePlacesFirst(favoriteKeys: Set<FavoriteKey>): List<Place> {
    val (favorites, rest) = partition { FavoriteKey(FavoriteItemType.PLACE, it.id) in favoriteKeys }
    return favorites + rest
}

fun List<Experience>.favoriteExperiencesFirst(favoriteKeys: Set<FavoriteKey>): List<Experience> {
    val (favorites, rest) = partition { FavoriteKey(FavoriteItemType.ACTIVITY, it.id) in favoriteKeys }
    return favorites + rest
}

fun List<NearbyPlace>.favoriteNearbyFirst(favoriteKeys: Set<FavoriteKey>): List<NearbyPlace> {
    val (favorites, rest) = partition { FavoriteKey(FavoriteItemType.PLACE, it.place.id) in favoriteKeys }
    return favorites + rest
}
