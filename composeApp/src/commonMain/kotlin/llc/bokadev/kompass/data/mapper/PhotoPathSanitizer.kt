package llc.bokadev.kompass.data.mapper

internal fun List<String>.sanitizePhotoPaths(): List<String> = this
    .map(String::trim)
    .filter { it.isNotBlank() }
    .distinct()
