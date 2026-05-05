package llc.bokadev.kompass.domain.repository

interface AnalyticsRepository {
    fun trackScreenView(screenName: String)
    fun trackCategoryView(categoryKey: String, contentOrigin: String = "browse")
    fun trackPlaceView(
        placeId: String,
        cityId: String? = null,
        zone: String? = null,
        placeCategory: String? = null,
        contentOrigin: String? = null
    )
    fun trackActivityView(
        activityId: String,
        cityId: String? = null,
        zone: String? = null,
        contentOrigin: String? = null
    )
    fun trackEventView(
        eventId: String,
        contentOrigin: String? = null
    )
    fun trackInfoNoticeView(
        noticeId: String,
        cityId: String? = null,
        contentOrigin: String? = null
    )
    fun trackNearbyView(zone: String? = null)
    fun trackGuideOpen(activityId: String, cityId: String? = null)
    fun trackAudioPlay(activityId: String, cityId: String? = null)
    fun trackPremiumBundleOpen(contentOrigin: String? = null)
}
