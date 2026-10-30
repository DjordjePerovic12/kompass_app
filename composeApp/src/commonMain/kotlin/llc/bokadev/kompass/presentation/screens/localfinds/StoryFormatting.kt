package llc.bokadev.kompass.presentation.screens.localfinds

import llc.bokadev.kompass.domain.model.LocalFind
import kotlin.math.ceil

/** UI labels only; the CMS has no kind column yet, so every story is a legend until it does. */
enum class StoryKind { LEGEND, LOCALISM }

/** Story list card shapes (stories spec §1.3), decided per story, never per screen. */
enum class StoryCardLayout { FEATURE, COMPACT, QUOTE }

/** TODO(CMS): read `kind` once `local_finds` has it. Until then a story is a legend. */
val LocalFind.storyKind: StoryKind get() = StoryKind.LEGEND

/** First story = feature; a legend with a photo = compact; a localism or no photo = quote. */
internal fun cardLayoutFor(index: Int, kind: StoryKind, hasPhoto: Boolean): StoryCardLayout = when {
    index == 0 -> StoryCardLayout.FEATURE
    hasPhoto && kind == StoryKind.LEGEND -> StoryCardLayout.COMPACT
    else -> StoryCardLayout.QUOTE
}

/** Words ÷ 200, rounded up, at least 1; null when there is no body (the read time hides). */
internal fun readMinutes(body: String): Int? {
    val words = body.split(Regex("\\s+")).count { it.isNotBlank() }
    if (words == 0) return null
    return ceil(words / 200.0).toInt().coerceAtLeast(1)
}

/**
 * Paragraphs: a blank line always breaks; a single line break only when the line ends a sentence
 * (the CMS also has hard-wrapped lines mid-sentence, which are joined with a space). A "> " line
 * (pull quote) always stands alone.
 */
internal fun storyParagraphs(body: String): List<String> {
    val paragraphs = mutableListOf<String>()
    body.replace("\r\n", "\n").split(Regex("\n\\s*\n")).forEach { block ->
        var current = StringBuilder()
        block.split('\n').map { it.trim() }.filter { it.isNotBlank() }.forEach { line ->
            val quote = line.startsWith(">")
            if (quote || current.startsWith(">")) {
                if (current.isNotEmpty()) paragraphs += current.toString()
                current = StringBuilder(line)
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(line)
            }
            if (quote || line.last() in SENTENCE_END) {
                paragraphs += current.toString()
                current = StringBuilder()
            }
        }
        if (current.isNotEmpty()) paragraphs += current.toString()
    }
    return paragraphs
}

private val SENTENCE_END = setOf('.', '!', '?', '…', '"', '\u201D', ':')

/** The excerpt: the first sentence of the body (no short-description field in the CMS yet). Never generated. */
internal fun storyExcerpt(body: String): String? {
    val first = storyParagraphs(body).firstOrNull()?.removePrefix(">")?.trim() ?: return null
    val end = Regex("[.!?…](\\s|$)").find(first)?.range?.first
    return (if (end != null) first.substring(0, end + 1) else first).trim().takeIf { it.isNotBlank() }
}

private val LOCATION_SEPARATORS = Regex("\\s+[—–-]\\s+")

/** The area for the `LEGEND · AREA` label: the location text's first segment, only if it's short. */
internal fun storyArea(location: String): String? =
    location.split(LOCATION_SEPARATORS).firstOrNull()?.trim()?.trimEnd('.', ',')
        ?.takeIf { it.isNotBlank() && it.length <= 24 }

/** "Old Town Kotor — Karampana Wall & Fairy's Gate" → "Old Town Kotor: Karampana Wall & Fairy's Gate". */
internal fun cleanStoryLocation(location: String): String? {
    val parts = location.split(LOCATION_SEPARATORS).map { it.trim() }.filter { it.isNotBlank() }
    if (parts.isEmpty()) return null
    return if (parts.size == 1) parts[0] else parts[0] + ": " + parts.drop(1).joinToString(", ")
}

/** A short place name from the location text (its last segment), or null when there isn't one. */
internal fun storyPlaceName(location: String): String? =
    location.split(LOCATION_SEPARATORS).lastOrNull()?.trim()?.trimEnd('.')?.takeIf { it.isNotBlank() && it.length <= 32 }

/** A paragraph the CMS marked as a pull quote by starting it with "> ". */
internal fun pullQuoteOf(paragraph: String): String? =
    paragraph.takeIf { it.startsWith(">") }?.removePrefix(">")?.trim()?.takeIf { it.isNotBlank() }
