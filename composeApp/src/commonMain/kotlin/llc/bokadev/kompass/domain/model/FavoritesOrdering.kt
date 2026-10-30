package llc.bokadev.kompass.domain.model

fun Place.isMarkedMustSee(): Boolean {
    if (isMustSee) return true
    return tags.any { tag ->
        val normalized = tag.trim().lowercase().replace('-', '_').replace(' ', '_')
        normalized == "must_see"
    }
}
