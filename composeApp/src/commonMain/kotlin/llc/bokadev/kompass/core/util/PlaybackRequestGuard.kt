package llc.bokadev.kompass.core.util

/**
 * Supersedes a pending audio prepare() call whose async setup (signed-URL fetch, location
 * lookup, etc.) completes after it no longer applies: an in-app destination change, a tab
 * switch, or a newer playback selection. A screen's own lifecycle observer cannot be trusted
 * alone here — removing a tab's NavHost does not guarantee ON_PAUSE reaches the destination
 * still holding a pending request.
 */
class PlaybackRequestGuard {
    private var generation: Long = 0L

    /** Call before starting async work that may end in prepare(). Supersedes any prior token. */
    fun newToken(): Long {
        generation += 1
        return generation
    }

    /** True if [token] is still the most recent one issued — safe to call prepare(). */
    fun isCurrent(token: Long): Boolean = token == generation

    /** Supersedes every token issued so far, without issuing a new one of its own. */
    fun invalidate() {
        generation += 1
    }
}
