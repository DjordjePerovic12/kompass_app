package llc.bokadev.kompass.core.util

import java.net.URLEncoder

actual fun buildMapsUrl(query: String): String {
    val encoded = URLEncoder.encode(query, "UTF-8")
    return "geo:0,0?q=$encoded"
}
