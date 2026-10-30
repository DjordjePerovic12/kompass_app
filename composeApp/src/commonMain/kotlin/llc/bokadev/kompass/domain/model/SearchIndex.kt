package llc.bokadev.kompass.domain.model

/** Searchable content types, in the order the spec lists them (also the "All" tie-break order). */
enum class SearchKind { PLACE, ACTIVITY, STORY, ESSENTIAL, EVENT, UTILITY, SERVICE, NOTICE }

/** The independently loaded sources the index is built from; a failed one makes results incomplete. */
enum class SearchSource { PLACES, ACTIVITIES, STORIES, ESSENTIALS, EVENTS, UTILITIES, SERVICES, NOTICES }

/**
 * What an item *is*, for matching words absent from its title ("restaurant" → a seafood place).
 * Turned into localized words by [SearchVocabulary]; never shown.
 */
sealed interface SearchTag {
    data class Place(val category: PlaceCategory) : SearchTag
    /** CMS `sub_category` (places) or `category` (activities), normalized: lowercase, spaces for `_`/`-`. */
    data class Subtype(val value: String) : SearchTag
    data class Essential(val category: EssentialCategory) : SearchTag
    data class Utility(val category: UtilityCategory) : SearchTag
}

/**
 * The reviewed search vocabulary for one language (English words always included, as visitors
 * often type English). Lives outside UI code so it stays one maintainable table.
 */
interface SearchVocabulary {
    /** Words a visitor might type for [tag]: its localized label plus a few plain synonyms. */
    fun terms(tag: SearchTag, lang: String): List<String>
    /** A whole query → other words to try (e.g. "airport" → taxi, bus, transfer). Directional. */
    fun related(lang: String): Map<String, List<String>>
}

/** The built index: items, related-term table and which sources failed (results may be incomplete). */
class SearchIndex(
    val items: List<SearchIndexItem>,
    related: Map<String, List<String>> = emptyMap(),
    val failedSources: Set<SearchSource> = emptySet()
) {
    internal val relatedFolded: Map<String, List<String>> =
        related.entries.associate { (k, v) -> foldForSearch(k).text to v.map { foldForSearch(it).text }.filter { it.length >= SEARCH_MIN_CHARS } }
    val isComplete: Boolean get() = failedSources.isEmpty()
}

/**
 * One entry of the local search index, built from cached content. [name] is in the app language
 * (English fallback); [area] (e.g. "Old Town") is a weaker match and part of the context line.
 * The other fields are raw so the UI can format a translated context line.
 */
data class SearchIndexItem(
    val kind: SearchKind,
    val id: String,
    val name: String,
    val area: String? = null,
    val photo: String? = null,
    val placeCategory: PlaceCategory? = null,
    val subtype: String? = null,
    val essentialCategory: EssentialCategory? = null,
    val eventStart: String? = null,
    /** Other names to match (e.g. the English name when the app language differs). Not highlighted. */
    val aliases: List<String> = emptyList(),
    /** Localized category/subtype words (from [SearchTag]s). Matched by word prefix, not highlighted. */
    val keywords: List<String> = emptyList(),
    val utilityCategory: UtilityCategory? = null,
    /**
     * Number of points for a utility *category* entry ("Cash & ATMs · 53 on the map"); null for
     * everything else. Single utility points match by name/address only, so a category word
     * gives one entry instead of dozens of identical rows.
     */
    val pointCount: Int? = null,
    /** False for single utility points: category words and related words don't match them. */
    val matchesByMeaning: Boolean = true
) {
    internal val nameFolded: FoldedText by lazy { foldForSearch(name) }
    internal val aliasesFolded: List<String> by lazy { aliases.map { foldForSearch(it).text } }
    internal val areaFolded: String by lazy { foldForSearch(area.orEmpty()).text }
    internal val keywordsFolded: List<String> by lazy { keywords.map { foldForSearch(it).text } }
}

/**
 * A match: [start]/[length] are positions in the item's *original* name (−1 = no highlight).
 * Scores: 0 name prefix, 1 word prefix, 2 substring, 3 alias, 4 category/subtype word,
 * 5 related word (vocabulary, name or category), 6 area.
 */
data class SearchHit(val item: SearchIndexItem, val score: Int, val start: Int, val length: Int)

const val SEARCH_MIN_CHARS = 2
const val SEARCH_LIMIT = 50

/**
 * Local, destination-only search (search spec §3): fold diacritics and case on both sides, then
 * rank full-name prefix > word prefix > substring > alias > category word > related word > area;
 * ties by type order, then name. Deterministic: no network, no learned ranking.
 */
fun searchIndex(query: String, index: SearchIndex, limit: Int = Int.MAX_VALUE): List<SearchHit> {
    val q = foldForSearch(query).text.trim()
    if (q.length < SEARCH_MIN_CHARS) return emptyList()
    val related = index.relatedFolded[q].orEmpty()
    return index.items.mapNotNull { item ->
        val folded = item.nameFolded
        val i = folded.text.indexOf(q)
        // Prefer a word-start occurrence ("pi" → "Pima" in "The spirit of Pima", not "spirit").
        val wordStart = if (i > 0) folded.text.wordStartIndexOf(q, i) else -1
        when {
            i == 0 -> item.hit(0, folded, i, q.length)
            wordStart > 0 -> item.hit(1, folded, wordStart, q.length)
            i > 0 -> item.hit(2, folded, i, q.length)
            item.aliasesFolded.any { it.contains(q) } -> SearchHit(item, 3, -1, 0)
            item.matchesByMeaning && item.keywordsFolded.any { it.hasWordPrefix(q) } -> SearchHit(item, 4, -1, 0)
            item.matchesByMeaning && related.any { r -> folded.text.hasWordPrefix(r) || item.keywordsFolded.any { it.hasWordPrefix(r) } } -> SearchHit(item, 5, -1, 0)
            item.areaFolded.contains(q) -> SearchHit(item, 6, -1, 0)
            else -> null
        }
    }
        .sortedWith(compareBy<SearchHit>({ it.score }, { it.item.kind.ordinal }, { it.item.name.lowercase() }))
        .take(limit)
}

fun searchIndex(query: String, items: List<SearchIndexItem>, limit: Int = Int.MAX_VALUE): List<SearchHit> =
    searchIndex(query, SearchIndex(items), limit)

/** [q] starts this text or one of its words ("rest" → "fish restaurant"). */
private fun String.hasWordPrefix(q: String): Boolean = startsWith(q) || contains(" $q")

private fun String.wordStartIndexOf(q: String, from: Int): Int {
    var i = indexOf(q, from)
    while (i > 0) {
        if (!this[i - 1].isLetterOrDigit()) return i
        i = indexOf(q, i + 1)
    }
    return -1
}

private fun SearchIndexItem.hit(score: Int, folded: FoldedText, start: Int, length: Int): SearchHit {
    val originalStart = folded.origin[start]
    val originalEnd = folded.origin.getOrElse(start + length) { name.length }
    return SearchHit(this, score, originalStart, originalEnd - originalStart)
}

/** Folded text plus, for each folded char, the index of the original char it came from. */
class FoldedText(val text: String, val origin: IntArray)

private val foldMap: Map<Char, String> = buildMap {
    fun put(chars: String, to: String) = chars.forEach { put(it, to) }
    put("àáâãäåāăą", "a"); put("çćĉċč", "c"); put("ďđ", "d"); put("èéêëēĕėęě", "e")
    put("ĝğġģ", "g"); put("ĥħ", "h"); put("ìíîïĩīĭįıİ", "i"); put("ĵ", "j"); put("ķ", "k"); put("ĺļľŀł", "l")
    put("ñńņňŉ", "n"); put("òóôõöøōŏő", "o"); put("ŕŗř", "r"); put("śŝşšș", "s"); put("ţťŧț", "t")
    put("ùúûüũūŭůűų", "u"); put("ŵ", "w"); put("ýÿŷ", "y"); put("źżž", "z"); put("ß", "ss"); put("æ", "ae"); put("œ", "oe")
}

/** Lowercase, strip Latin diacritics (đ → d, ć → c, …), collapse whitespace; keeps an origin map. */
fun foldForSearch(input: String): FoldedText {
    val out = StringBuilder()
    val origin = ArrayList<Int>()
    var lastWasSpace = true
    input.forEachIndexed { index, raw ->
        val lower = raw.lowercaseChar()
        val mapped = foldMap[lower] ?: foldMap[raw] ?: lower.toString()
        if (mapped.isBlank()) {
            if (!lastWasSpace) { out.append(' '); origin.add(index) }
            lastWasSpace = true
        } else {
            mapped.forEach { out.append(it); origin.add(index) }
            lastWasSpace = false
        }
    }
    if (out.endsWith(' ')) { out.setLength(out.length - 1); origin.removeAt(origin.lastIndex) }
    return FoldedText(out.toString(), origin.toIntArray())
}
