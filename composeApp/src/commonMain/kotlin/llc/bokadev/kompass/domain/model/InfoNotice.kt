@file:OptIn(kotlin.time.ExperimentalTime::class)

package llc.bokadev.kompass.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

data class InfoNotice(
    val id: String,
    val cityId: String,
    val title: Map<String, String>,
    val shortDescription: Map<String, String>,
    val longDescription: Map<String, String>? = null,
    val imageUrl: String? = null,
    val priority: String = "general",
    val noticeType: String = "general",
    val startsAt: String? = null,
    val endsAt: String? = null,
    val location: Map<String, String>? = null,
    val externalUrl: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun localizedTitle(lang: String): String = title[lang] ?: title["en"] ?: ""
    fun localizedShortDescription(lang: String): String = shortDescription[lang] ?: shortDescription["en"] ?: ""
    fun localizedLongDescription(lang: String): String = longDescription?.get(lang) ?: longDescription?.get("en") ?: ""
    fun localizedLocation(lang: String): String = location?.get(lang) ?: location?.get("en") ?: ""

    fun isCurrent(now: Instant = Clock.System.now()): Boolean {
        if (!isActive) return false
        val starts = startsAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
        val ends = endsAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
        if (starts != null && starts > now) return false
        if (ends != null && ends < now) return false
        return true
    }

    fun priorityRank(): Int = when (priority.lowercase()) {
        "urgent" -> 0
        "important" -> 1
        else -> 2
    }

    fun recencyKey(): String = startsAt ?: createdAt ?: updatedAt ?: ""
}
