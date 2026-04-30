package llc.bokadev.kompass.domain.model

data class EventFilter(
    val id: String,
    val key: String,
    val kind: EventFilterKind,
    val label: Map<String, String>,
    val value: String,
    val sortOrder: Int,
    val isActive: Boolean
) {
    fun localizedLabel(lang: String): String = label[lang] ?: label["en"] ?: value
}

enum class EventFilterKind {
    DATE,
    TYPE
}
