package llc.bokadev.kompass.core.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackRequestGuardTest {

    @Test
    fun tokenStaysCurrentUntilSuperseded() {
        val guard = PlaybackRequestGuard()
        val token = guard.newToken()
        assertTrue(guard.isCurrent(token))
    }

    @Test
    fun newerSelectionSupersedesOlderPendingOne() {
        val guard = PlaybackRequestGuard()
        val first = guard.newToken()
        val second = guard.newToken()
        assertFalse(guard.isCurrent(first))
        assertTrue(guard.isCurrent(second))
    }

    @Test
    fun invalidateRejectsAnyPendingToken() {
        val guard = PlaybackRequestGuard()
        val token = guard.newToken()
        guard.invalidate()
        assertFalse(guard.isCurrent(token))
    }

    @Test
    fun invalidateWithNoPendingTokenStillSupersedesTheNextOne() {
        val guard = PlaybackRequestGuard()
        guard.invalidate()
        val token = guard.newToken()
        assertTrue(guard.isCurrent(token))
    }
}
