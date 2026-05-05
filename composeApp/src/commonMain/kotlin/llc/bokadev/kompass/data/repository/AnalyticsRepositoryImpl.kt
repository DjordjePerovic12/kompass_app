package llc.bokadev.kompass.data.repository

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import llc.bokadev.kompass.BuildKonfig
import llc.bokadev.kompass.getPlatform
import llc.bokadev.kompass.core.util.AppPreferences
import llc.bokadev.kompass.domain.model.AnalyticsEvent
import llc.bokadev.kompass.domain.repository.AnalyticsRepository
import kotlin.random.Random
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AnalyticsRepositoryImpl(
    private val httpClient: HttpClient,
    private val preferences: AppPreferences
) : AnalyticsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessionId = buildSessionId()
    private val platform = platformName()
    private val defaultCitySlug = "kotor"

    override fun trackScreenView(screenName: String) {
        sendEvent(
            AnalyticsEvent(
                eventName = "screen_view",
                citySlug = defaultCitySlug,
                appLanguage = preferences.getSelectedLanguage(),
                appVersion = BuildKonfig.APP_VERSION,
                platform = platform,
                anonymousUserId = anonymousUserId(),
                sessionId = sessionId,
                contentOrigin = screenName
            )
        )
    }

    override fun trackCategoryView(categoryKey: String, contentOrigin: String) {
        sendEvent(
            baseEvent("category_view").copy(
                categoryKey = categoryKey,
                contentOrigin = contentOrigin
            )
        )
    }

    override fun trackPlaceView(
        placeId: String,
        cityId: String?,
        zone: String?,
        placeCategory: String?,
        contentOrigin: String?
    ) {
        sendEvent(
            baseEvent("place_view").copy(
                cityId = cityId,
                placeId = placeId,
                zone = zone,
                placeCategory = placeCategory,
                contentOrigin = contentOrigin
            )
        )
    }

    override fun trackActivityView(activityId: String, cityId: String?, zone: String?, contentOrigin: String?) {
        sendEvent(
            baseEvent("activity_view").copy(
                cityId = cityId,
                activityId = activityId,
                zone = zone,
                contentOrigin = contentOrigin
            )
        )
    }

    override fun trackEventView(eventId: String, contentOrigin: String?) {
        sendEvent(
            baseEvent("event_view").copy(
                eventId = eventId,
                contentOrigin = contentOrigin
            )
        )
    }

    override fun trackInfoNoticeView(noticeId: String, cityId: String?, contentOrigin: String?) {
        sendEvent(
            baseEvent("info_notice_view").copy(
                cityId = cityId,
                infoNoticeId = noticeId,
                contentOrigin = contentOrigin
            )
        )
    }

    override fun trackNearbyView(zone: String?) {
        sendEvent(
            baseEvent("nearby_view").copy(
                zone = zone,
                contentOrigin = "nearby"
            )
        )
    }

    override fun trackGuideOpen(activityId: String, cityId: String?) {
        sendEvent(
            baseEvent("guide_open").copy(
                cityId = cityId,
                activityId = activityId,
                contentOrigin = "guide"
            )
        )
    }

    override fun trackAudioPlay(activityId: String, cityId: String?) {
        sendEvent(
            baseEvent("audio_play").copy(
                cityId = cityId,
                activityId = activityId,
                contentOrigin = "guide_audio"
            )
        )
    }

    override fun trackPremiumBundleOpen(contentOrigin: String?) {
        sendEvent(
            baseEvent("premium_bundle_open").copy(
                contentOrigin = contentOrigin ?: "premium"
            )
        )
    }

    private fun baseEvent(eventName: String): AnalyticsEvent {
        return AnalyticsEvent(
            eventName = eventName,
            citySlug = defaultCitySlug,
            appLanguage = preferences.getSelectedLanguage(),
            appVersion = BuildKonfig.APP_VERSION,
            platform = platform,
            anonymousUserId = anonymousUserId(),
            sessionId = sessionId
        )
    }

    private fun sendEvent(event: AnalyticsEvent) {
        scope.launch {
            runCatching {
                httpClient.post("${analyticsBaseUrl().trimEnd('/')}/analytics-ingest") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${supabaseAnonKey()}")
                    header("apikey", supabaseAnonKey())
                    setBody(event)
                }.body<String>()
            }.onFailure {
                Napier.w("Analytics event failed: ${event.eventName} - ${it.message}")
            }
        }
    }

    private fun anonymousUserId(): String {
        preferences.getAnonymousUserId()?.let { return it }
        val generated = "anon-${Random.nextLong().toString(16)}-${kotlin.time.Clock.System.now().toEpochMilliseconds()}"
        preferences.setAnonymousUserId(generated)
        return generated
    }

    private fun analyticsBaseUrl(): String {
        return BuildKonfig.ANALYTICS_BACKEND_BASE_URL
            .takeIf { it.isNotBlank() }
            ?: "${BuildKonfig.SUPABASE_URL}/functions/v1"
    }

    private fun supabaseAnonKey(): String {
        return if (BuildKonfig.USE_LOCAL_SUPABASE && BuildKonfig.SUPABASE_ANON_KEY_LOCAL.isNotBlank()) {
            BuildKonfig.SUPABASE_ANON_KEY_LOCAL
        } else {
            BuildKonfig.SUPABASE_ANON_KEY
        }
    }

    private fun buildSessionId(): String =
        "session-${Random.nextLong().toString(16)}-${kotlin.time.Clock.System.now().toEpochMilliseconds()}"

    private fun platformName(): String = getPlatform().name.lowercase()
}
