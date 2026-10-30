package llc.bokadev.kompass

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.SystemClock
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import llc.bokadev.kompass.core.util.AudioGuidePlaybackState

class AudioGuidePlaybackService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null

    /** Set when a transient focus loss (call, navigation prompt) paused us while playing. */
    private var resumeOnFocusGainUntil: Long? = null
    private var noisyReceiverRegistered = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (mediaPlayer?.isPlaying == true) {
                    pausePlayback()
                    resumeOnFocusGainUntil = SystemClock.elapsedRealtime() + RESUME_WINDOW_MS
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                val deadline = resumeOnFocusGainUntil
                resumeOnFocusGainUntil = null
                if (deadline != null && SystemClock.elapsedRealtime() <= deadline) resumePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS -> stopPlayback()
        }
    }

    /** Headphones unplugged / Bluetooth gone: pause rather than blast the speaker. */
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY && mediaPlayer?.isPlaying == true) {
                pausePlayback()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSession = MediaSessionCompat(this, "KompassAudioGuides").apply {
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PREPARE -> prepare(
                url = intent.getStringExtra(EXTRA_URL).orEmpty(),
                title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
                subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
            )

            ACTION_TOGGLE -> togglePlayback()
            ACTION_SEEK -> seekTo(intent.getLongExtra(EXTRA_POSITION_MS, 0L))
            ACTION_STOP -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlayback()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        abandonFocus()
        unregisterNoisy()
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession.isActive = false
        mediaSession.release()
        serviceScope.cancel()
        playbackState.value = AudioGuidePlaybackState()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun prepare(url: String, title: String, subtitle: String) {
        if (url.isBlank()) return

        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null

        playbackState.value = AudioGuidePlaybackState(
            sourceUrl = url,
            title = title,
            subtitle = subtitle,
            isBuffering = true
        )
        updateMediaSession()
        startForeground(NOTIFICATION_ID, buildNotification(title, subtitle, isPlaying = false))

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener { player ->
                if (!requestFocus()) {
                    // Another app holds exclusive focus (e.g. a call): stay prepared and paused.
                    playbackState.value = playbackState.value.copy(
                        isBuffering = false,
                        durationMs = player.duration.toLong(),
                        progressMs = 0L
                    )
                    updateMediaSession()
                    updateNotification()
                    return@setOnPreparedListener
                }
                registerNoisy()
                player.start()
                playbackState.value = playbackState.value.copy(
                    isPlaying = true,
                    isBuffering = false,
                    durationMs = player.duration.toLong(),
                    progressMs = player.currentPosition.toLong(),
                    error = null
                )
                updateMediaSession()
                updateNotification()
                startProgressLoop()
            }
            setOnCompletionListener {
                unregisterNoisy()
                playbackState.value = playbackState.value.copy(
                    isPlaying = false,
                    progressMs = playbackState.value.durationMs,
                    isCompleted = true
                )
                updateMediaSession()
                updateNotification()
                progressJob?.cancel()
            }
            setOnErrorListener { _, _, _ ->
                playbackState.value = playbackState.value.copy(
                    isPlaying = false,
                    isBuffering = false,
                    error = "Audio could not be played."
                )
                updateMediaSession()
                updateNotification()
                true
            }
            prepareAsync()
        }
    }

    private fun togglePlayback() {
        val player = mediaPlayer ?: return
        resumeOnFocusGainUntil = null
        if (player.isPlaying) pausePlayback() else resumePlayback()
    }

    private fun pausePlayback() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) player.pause()
        unregisterNoisy()
        playbackState.value = playbackState.value.copy(isPlaying = false)
        updateMediaSession()
        updateNotification()
    }

    private fun resumePlayback() {
        val player = mediaPlayer ?: return
        if (!requestFocus()) return
        registerNoisy()
        player.start()
        playbackState.value = playbackState.value.copy(isPlaying = true, isCompleted = false)
        startProgressLoop()
        updateMediaSession()
        updateNotification()
    }

    private fun requestFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(focusListener)
                .build()
                .also { focusRequest = it }
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        resumeOnFocusGainUntil = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let(audioManager::abandonAudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }

    private fun registerNoisy() {
        if (noisyReceiverRegistered) return
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(noisyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(noisyReceiver, filter)
        }
        noisyReceiverRegistered = true
    }

    private fun unregisterNoisy() {
        if (!noisyReceiverRegistered) return
        runCatching { unregisterReceiver(noisyReceiver) }
        noisyReceiverRegistered = false
    }

    private fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        player.seekTo(positionMs.toInt())
        playbackState.value = playbackState.value.copy(progressMs = positionMs)
        updateMediaSession()
    }

    private fun stopPlayback() {
        abandonFocus()
        unregisterNoisy()
        progressJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        playbackState.value = AudioGuidePlaybackState()
        updateMediaSession()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (true) {
                val player = mediaPlayer ?: break
                playbackState.value = playbackState.value.copy(
                    progressMs = player.currentPosition.toLong(),
                    durationMs = player.duration.toLong().coerceAtLeast(0L),
                    isPlaying = player.isPlaying
                )
                updateMediaSession()
                delay(500)
            }
        }
    }

    private fun updateNotification() {
        val state = playbackState.value
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(state.title, state.subtitle, state.isPlaying))
    }

    private fun buildNotification(title: String, subtitle: String, isPlaying: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            1,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, AudioGuidePlaybackService::class.java).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, AudioGuidePlaybackService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title.ifBlank { "KOmpass Audio Guide" })
            .setContentText(subtitle.ifBlank { if (isPlaying) "Playing in background" else "Ready to play" })
            .setOngoing(isPlaying)
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOnlyAlertOnce(true)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                toggleIntent
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateMediaSession() {
        val state = playbackState.value
        val playbackStateValue = when {
            state.isBuffering -> PlaybackStateCompat.STATE_BUFFERING
            state.isPlaying -> PlaybackStateCompat.STATE_PLAYING
            state.sourceUrl == null -> PlaybackStateCompat.STATE_STOPPED
            else -> PlaybackStateCompat.STATE_PAUSED
        }

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(
                    playbackStateValue,
                    state.progressMs,
                    if (state.isPlaying) 1f else 0f
                )
                .build()
        )

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.subtitle)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, state.durationMs)
                .build()
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Audio Guide Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background playback for KOmpass audio guides"
            }
        )
    }

    companion object {
        private const val CHANNEL_ID = "kompass_audio_guides"
        private const val NOTIFICATION_ID = 4012

        /** Resume after a transient focus loss only if focus returns within this window (spec §2.3 rule 6). */
        private const val RESUME_WINDOW_MS = 30_000L

        private const val ACTION_PREPARE = "llc.bokadev.kompass.audio.PREPARE"
        private const val ACTION_TOGGLE = "llc.bokadev.kompass.audio.TOGGLE"
        private const val ACTION_SEEK = "llc.bokadev.kompass.audio.SEEK"
        private const val ACTION_STOP = "llc.bokadev.kompass.audio.STOP"

        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_SUBTITLE = "extra_subtitle"
        private const val EXTRA_POSITION_MS = "extra_position_ms"

        private val playbackState = MutableStateFlow(AudioGuidePlaybackState())

        fun stateFlow() = playbackState.asStateFlow()

        fun prepareIntent(context: Context, url: String, title: String, subtitle: String) =
            Intent(context, AudioGuidePlaybackService::class.java).apply {
                action = ACTION_PREPARE
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
            }

        fun toggleIntent(context: Context) =
            Intent(context, AudioGuidePlaybackService::class.java).apply { action = ACTION_TOGGLE }

        fun seekIntent(context: Context, positionMs: Long) =
            Intent(context, AudioGuidePlaybackService::class.java).apply {
                action = ACTION_SEEK
                putExtra(EXTRA_POSITION_MS, positionMs)
            }

        fun stopIntent(context: Context) =
            Intent(context, AudioGuidePlaybackService::class.java).apply { action = ACTION_STOP }
    }
}
