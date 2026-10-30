package llc.bokadev.kompass.core.utils

import llc.bokadev.kompass.BuildKonfig

fun buildPhotoUrl(path: String, bucket: String = "places"): String =
    if (path.startsWith("http")) path
    else "${BuildKonfig.SUPABASE_URL}/storage/v1/object/public/$bucket/$path"
