package llc.bokadev.kompass.core.audio

/** What a tap does on a control that mirrors the center button (place detail spec §2.5 table). */
fun AudioController.onNavAudioTap(mode: NavAudioMode, owner: String, onUnavailable: () -> Unit) {
    when (mode) {
        NavAudioMode.NoAudio -> Unit
        is NavAudioMode.Ready -> play(mode.track, owner)
        is NavAudioMode.Failed -> play(mode.track, owner)
        is NavAudioMode.Paused -> play(mode.track, owner)
        is NavAudioMode.Playing -> pause()
        is NavAudioMode.Loading -> cancelLoading(owner)
        is NavAudioMode.Unavailable -> onUnavailable()
    }
}
