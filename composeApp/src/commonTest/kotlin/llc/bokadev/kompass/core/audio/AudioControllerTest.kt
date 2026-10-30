package llc.bokadev.kompass.core.audio

import llc.bokadev.kompass.core.utils.networking.NetworkError
import llc.bokadev.kompass.core.utils.networking.Resource
import llc.bokadev.kompass.domain.model.AudioSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import llc.bokadev.kompass.core.utils.AudioGuidePlaybackState
import llc.bokadev.kompass.core.utils.AudioGuidePlayer
import llc.bokadev.kompass.core.utils.PlaybackRequestGuard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AudioControllerTest {

    private val cathedral = track("cathedral")
    private val church = track("church")

    @Test fun playResolvesThenPreparesAndReportsPlaying() = runTest {
        val env = env()
        env.controller.play(cathedral, owner = "a")
        assertIs<AudioState.Loading>(env.controller.state.value)
        runCurrent()
        assertEquals(listOf("url:cathedral"), env.player.prepared)
        env.player.emitPlaying("url:cathedral", positionMs = 1_000, durationMs = 60_000)
        runCurrent()
        val playing = assertIs<AudioState.Playing>(env.controller.state.value)
        assertEquals(1_000, playing.positionMs)
        assertEquals(60_000, playing.durationMs)
    }

    @Test fun leavingBeforeTheSlowLoadFinishesNeverPlays() = runTest {
        val slow = CompletableDeferred<Resource<String, NetworkError, String?>>()
        val env = env(resolve = { slow.await() })
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.controller.stopIfOwner("a")
        slow.complete(Resource.Success("url:cathedral"))
        runCurrent()
        assertTrue(env.player.prepared.isEmpty())
        assertEquals(AudioState.Idle, env.controller.state.value)
    }

    @Test fun navigationInvalidatingTheGuardDropsAPendingLoad() = runTest {
        val slow = CompletableDeferred<Resource<String, NetworkError, String?>>()
        val env = env(resolve = { slow.await() })
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.guard.invalidate() // what MainShell does on any destination change / tab switch
        slow.complete(Resource.Success("url:cathedral"))
        runCurrent()
        assertTrue(env.player.prepared.isEmpty())
        assertEquals(AudioState.Idle, env.controller.state.value)
    }

    @Test fun onlyTheSecondTrackPlaysWhenTheFirstLoadIsStillPending() = runTest {
        val first = CompletableDeferred<Resource<String, NetworkError, String?>>()
        val env = env(resolve = { path -> if (path == "cathedral.mp3") first.await() else Resource.Success("url:church") })
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.controller.play(church, owner = "b")
        runCurrent()
        first.complete(Resource.Success("url:cathedral"))
        runCurrent()
        assertEquals(listOf("url:church"), env.player.prepared)
    }

    @Test fun pauseAndResumeToggleTheSameTrackWithoutReloading() = runTest {
        val env = env()
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.player.emitPlaying("url:cathedral", 5_000, 60_000)
        runCurrent()
        env.controller.pause()
        runCurrent()
        assertIs<AudioState.Paused>(env.controller.state.value)
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        assertIs<AudioState.Playing>(env.controller.state.value)
        assertEquals(1, env.player.prepared.size)
    }

    @Test fun trackEndReturnsToIdleSoTheButtonIsReadyAgain() = runTest {
        val env = env()
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.player.emitPlaying("url:cathedral", 59_000, 60_000)
        runCurrent()
        env.player.state.value = env.player.state.value.copy(isPlaying = false, isCompleted = true)
        runCurrent()
        assertEquals(AudioState.Idle, env.controller.state.value)
        assertEquals(1, env.player.stops)
    }

    @Test fun resolveFailureAndPlayerErrorBothSurfaceAsFailed() = runTest {
        val env = env(resolve = { Resource.Error(NetworkError.UNKNOWN, "offline") })
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        assertEquals(AudioState.Reason.Network, assertIs<AudioState.Failed>(env.controller.state.value).reason)

        val env2 = env()
        env2.controller.play(cathedral, owner = "a")
        runCurrent()
        env2.player.state.value = AudioGuidePlaybackState(sourceUrl = "url:cathedral", error = "boom")
        runCurrent()
        assertEquals(AudioState.Reason.Decode, assertIs<AudioState.Failed>(env2.controller.state.value).reason)
    }

    @Test fun anExternalStopReturnsToIdle() = runTest {
        val env = env()
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.player.emitPlaying("url:cathedral", 1_000, 60_000)
        runCurrent()
        env.player.state.value = AudioGuidePlaybackState() // notification "Stop" / MainShell stop
        runCurrent()
        assertEquals(AudioState.Idle, env.controller.state.value)
    }

    @Test fun aLeavingScreenThatNoLongerOwnsPlaybackDoesNotStopTheNewOne() = runTest {
        val env = env()
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.controller.play(church, owner = "b")
        runCurrent()
        env.player.emitPlaying("url:church", 1_000, 60_000)
        runCurrent()
        env.controller.stopIfOwner("a")
        runCurrent()
        assertIs<AudioState.Playing>(env.controller.state.value)
    }

    @Test fun buttonModeFollowsOnlyItsOwnTrackAndOfflineAvailability() {
        val ctx = NavAudioContext("a", cathedral)
        assertEquals(NavAudioMode.NoAudio, resolveNavAudioMode(NavAudioContext("a", null), AudioState.Idle, offline = false))
        assertIs<NavAudioMode.Ready>(resolveNavAudioMode(ctx, AudioState.Idle, offline = false))
        assertIs<NavAudioMode.Ready>(resolveNavAudioMode(ctx, AudioState.Playing(church, 0, null), offline = false))
        assertIs<NavAudioMode.Playing>(resolveNavAudioMode(ctx, AudioState.Playing(cathedral, 0, null), offline = false))
        assertIs<NavAudioMode.Unavailable>(resolveNavAudioMode(ctx, AudioState.Idle, offline = true))
        assertIs<NavAudioMode.Ready>(resolveNavAudioMode(NavAudioContext("a", cathedral.copy(isDownloaded = true)), AudioState.Idle, offline = true))
        assertEquals(0.5f, resolveNavAudioMode(ctx, AudioState.Paused(cathedral, 30_000, 60_000), offline = false).progress)
        assertEquals(null, resolveNavAudioMode(ctx, AudioState.Playing(cathedral, 30_000, null), offline = false).progress)
    }

    @Test fun everyVisitGetsTheFullPulseRun() {
        val host = NavAudioHost()
        assertEquals(listOf(5, 5, 5, 5), listOf("a", "b", "c", "d").map(host::pulseCyclesFor))
        host.push(NavAudioContext("a", cathedral))
        host.push(NavAudioContext("b", null))
        host.pop("b")
        assertEquals("a", host.current.value?.ownerKey)
    }

    private class Env(val controller: AudioController, val player: FakePlayer, val guard: PlaybackRequestGuard)

    @Test fun toggleOnTheCurrentTrackPausesAndOnAnotherReplacesIt() = runTest {
        val env = env()
        env.controller.toggle(cathedral, owner = "lib")
        runCurrent()
        env.player.emitPlaying("url:cathedral", 5_000, 60_000)
        runCurrent()
        env.controller.toggle(cathedral, owner = "lib")
        runCurrent()
        assertIs<AudioState.Paused>(env.controller.state.value)
        env.controller.toggle(church, owner = "lib")
        runCurrent()
        assertEquals(listOf("url:cathedral", "url:church"), env.player.prepared)
        assertEquals(church.id, env.controller.state.value.trackOrNull?.id)
    }

    @Test fun toggleWhileLoadingCancelsAndNothingPlaysLater() = runTest {
        val slow = CompletableDeferred<Resource<String, NetworkError, String?>>()
        val env = env(resolve = { slow.await() })
        env.controller.toggle(cathedral, owner = "lib")
        runCurrent()
        env.controller.toggle(cathedral, owner = "lib")
        slow.complete(Resource.Success("url:cathedral"))
        runCurrent()
        assertTrue(env.player.prepared.isEmpty())
        assertEquals(AudioState.Idle, env.controller.state.value)
    }

    @Test fun skipIsClampedToTheTrack() = runTest {
        val env = env()
        env.controller.skipBy(15_000) // nothing loaded: ignored
        env.controller.play(cathedral, owner = "a")
        runCurrent()
        env.player.emitPlaying("url:cathedral", 50_000, 60_000)
        runCurrent()
        env.controller.skipBy(15_000)
        env.controller.skipBy(-15_000)
        env.player.emitPlaying("url:cathedral", 5_000, 60_000)
        runCurrent()
        env.controller.skipBy(-15_000)
        env.controller.seekTo(90_000)
        assertEquals(listOf(60_000L, 35_000L, 0L, 60_000L), env.player.seeks)
    }

    private fun TestScope.env(
        resolve: suspend (String) -> Resource<String, NetworkError, String?> = { path -> Resource.Success("url:" + path.removeSuffix(".mp3")) }
    ): Env {
        val player = FakePlayer()
        val guard = PlaybackRequestGuard()
        return Env(AudioController(player, guard, resolve, backgroundScope), player, guard)
    }

    private fun track(id: String) = AudioTrack(
        source = AudioSource.PLACE, sourceId = id, title = id, subtitle = "", audioPath = "$id.mp3",
        lang = "en", isEnglishFallback = false, isDownloaded = false
    )

    private class FakePlayer : AudioGuidePlayer {
        val state = MutableStateFlow(AudioGuidePlaybackState())
        override val playbackState: StateFlow<AudioGuidePlaybackState> = state
        val prepared = mutableListOf<String>()
        var stops = 0

        override fun prepare(url: String, title: String, subtitle: String) {
            prepared += url
            state.value = AudioGuidePlaybackState(sourceUrl = url, title = title, isBuffering = true)
        }

        override fun togglePlayPause() {
            state.value = state.value.copy(isPlaying = !state.value.isPlaying)
        }

        val seeks = mutableListOf<Long>()
        override fun seekTo(positionMs: Long) { seeks += positionMs }

        override fun stop() {
            stops++
            state.value = AudioGuidePlaybackState()
        }

        fun emitPlaying(url: String, positionMs: Long, durationMs: Long) {
            state.value = AudioGuidePlaybackState(sourceUrl = url, isPlaying = true, progressMs = positionMs, durationMs = durationMs)
        }
    }
}
