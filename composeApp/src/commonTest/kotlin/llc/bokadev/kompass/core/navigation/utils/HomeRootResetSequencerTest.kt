package llc.bokadev.kompass.core.navigation.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import llc.bokadev.kompass.core.components.BottomTab

class HomeRootResetSequencerTest {

    @Test
    fun doesNotConsumeBeforeHomeIsSelected() {
        val sequencer = HomeRootResetSequencer()
        sequencer.arm()
        assertFalse(sequencer.consume(BottomTab.Explore))
        assertTrue(sequencer.isPending)
    }

    @Test
    fun consumesExactlyOnceOnceHomeIsSelected() {
        val sequencer = HomeRootResetSequencer()
        sequencer.arm()
        assertTrue(sequencer.consume(BottomTab.Home))
        assertFalse(sequencer.isPending)
        assertFalse(sequencer.consume(BottomTab.Home))
    }

    @Test
    fun unarmedSequencerNeverConsumes() {
        val sequencer = HomeRootResetSequencer()
        assertFalse(sequencer.consume(BottomTab.Home))
    }

    @Test
    fun recreationStartingInNonHomeTabDefersResetUntilHomeMounts() {
        // Simulates process recreation restoring `selected` into a non-Home tab (e.g. Explore),
        // then the user immediately pressing the root-level Back action. The old MainShell
        // popped home's back stack directly before switching tabs, which could crash because
        // Home's NavController never had its graph installed while another tab was selected.
        val sequencer = HomeRootResetSequencer()
        var selected = BottomTab.Explore
        var poppedWhileHomeWasUnmounted = false
        fun popHomeToRoot() {
            if (selected != BottomTab.Home) poppedWhileHomeWasUnmounted = true
        }

        // Root Back fires at the Explore root: arm the reset, then switch tabs — in that order.
        sequencer.arm()
        selected = BottomTab.Home

        // Only after the tab switch (and therefore Home's NavHost composition) settles does
        // the pop actually run.
        if (sequencer.consume(selected)) popHomeToRoot()

        assertFalse(poppedWhileHomeWasUnmounted)
        assertFalse(sequencer.isPending)
    }
}
