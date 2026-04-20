package llc.bokadev.kompass

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform