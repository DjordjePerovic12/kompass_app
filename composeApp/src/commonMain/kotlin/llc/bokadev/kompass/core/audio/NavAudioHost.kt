package llc.bokadev.kompass.core.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Which screen's audio the floating bar's center button controls. Screens push a context while
 * they're on top and pop it when they leave; the most recent push wins. Also decides how long
 * the "audio is here" pulse rings run per visit.
 */
class NavAudioHost {
    private val stack = mutableListOf<NavAudioContext>()
    private val _current = MutableStateFlow<NavAudioContext?>(null)
    val current: StateFlow<NavAudioContext?> = _current.asStateFlow()

    private val _interactionSignal = MutableStateFlow(0)
    /** Bumped when the user scrolls the top screen: stops the pulse rings and the hint bubble. */
    val interactionSignal: StateFlow<Int> = _interactionSignal.asStateFlow()

    fun notifyUserScrolled() {
        _interactionSignal.value += 1
    }

    fun push(context: NavAudioContext) {
        stack.removeAll { it.ownerKey == context.ownerKey }
        stack.add(context)
        _current.value = stack.lastOrNull()
    }

    fun pop(ownerKey: String) {
        stack.removeAll { it.ownerKey == ownerKey }
        _current.value = stack.lastOrNull()
    }

    /**
     * Pulse cycles for [ownerKey]'s visit. Every visit gets the full run: the spec's 3 cycles,
     * then 1 after three visits, proved easy to miss, so this is 5 cycles (~11s), never reduced.
     * The rings still stop at once on tap, scroll, or leaving.
     */
    @Suppress("UNUSED_PARAMETER")
    fun pulseCyclesFor(ownerKey: String): Int = PULSE_CYCLES

    companion object {
        const val PULSE_CYCLES = 5
    }
}
