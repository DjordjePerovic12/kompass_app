package llc.bokadev.kompass.core.util

actual fun buildMapsUrl(query: String): String {
    val encoded = query
        .replace(" ", "+")
        .replace("&", "%26")
        .replace("#", "%23")
    return "maps://?q=$encoded"
}
