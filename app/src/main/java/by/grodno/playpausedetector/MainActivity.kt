package by.grodno.playpausedetector

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import by.grodno.playpausedetector.ui.theme.PlayPauseDetectorTheme

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playPauseState by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player!!).setCallback(MediaSessionCallback()).build()
        playFake()
    }

    /**
     * Play short silence sound to make [MediaSession] active.
     * Active [MediaSession] can intercept media actions from Android system in [MediaSession.Callback.onMediaButtonEvent].
     * This is a workaround so if you know how to intercept play/pause buttons from headset without this - feel free to remove it.
     */
    private fun playFake() {
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .path(R.raw.one_second_of_silence.toString())
            .build()
        player?.setMediaItem(MediaItem.fromUri(uri))
        player?.prepare()
        player?.play()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onMediaButtonEvent(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            intent: Intent
        ): Boolean {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return false
            playPauseState = when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> "Play"
                KeyEvent.KEYCODE_MEDIA_PAUSE -> "Pause"
                else -> "Unknown KeyCode = $"
            }
            return super.onMediaButtonEvent(session, controllerInfo, intent)
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun buildUI() {
        setContent {
            PlayPauseDetectorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlayPauseStateText(state = playPauseState, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PlayPauseStateText(state: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = state, fontSize = 24.sp)
    }
}
