package llc.bokadev.kompass.presentation.navigation

/**
 * Sequences the explicit root-level Back action (switch to Home, then reset Home to its
 * root) so the reset never runs before Home is actually mounted. A tab's NavController only
 * has its graph installed while that tab is the one currently hosted by the shared NavHost —
 * popping Home's back stack before switching [BottomTab] to it can crash with no graph set,
 * e.g. when the process restores into a non-Home tab and root Back fires immediately.
 */
class HomeRootResetSequencer {
    var isPending: Boolean = false
        private set

    /** Arm the pending reset. Call this before switching the selected tab to Home. */
    fun arm() {
        isPending = true
    }

    /**
     * Call once per recomposition after the tab selection settles. Returns true exactly once
     * — when armed and [currentTab] is Home — meaning it is now safe to pop Home to its root.
     */
    fun consume(currentTab: BottomTab): Boolean {
        if (isPending && currentTab == BottomTab.Home) {
            isPending = false
            return true
        }
        return false
    }
}
