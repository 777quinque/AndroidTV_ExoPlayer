package ip.tomichek.tv

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.text.Subtitle
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegRendererFactory

class FullScreenPlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: StyledPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_player)

        playerView = findViewById(R.id.playerView)

        // Initialize the player
        initializePlayer()
    }

    private fun initializePlayer() {
        // Use FFmpeg renderer factory for enhanced format support
        val renderersFactory: RenderersFactory = FfmpegRendererFactory(this)

        player = ExoPlayer.Builder(this, renderersFactory).build()

        // Attach player to the UI
        playerView.player = player

        // Get video URL from intent
        val videoUrl = intent.getStringExtra("videoUrl") ?: return

        // Prepare media item with optional subtitle
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .build()

        player.setMediaItem(mediaItem)

        // Load subtitle if available
        val subtitleUrl = intent.getStringExtra("subtitleUrl")
        if (!subtitleUrl.isNullOrEmpty()) {
            val subtitle = MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl))
                .setMimeType("text/vtt") // Update if using a different subtitle format
                .build()
            val mediaItemWithSubtitle = MediaItem.Builder()
                .setUri(Uri.parse(videoUrl))
                .setSubtitleConfigurations(listOf(subtitle))
                .build()
            player.setMediaItem(mediaItemWithSubtitle)
        }

        player.prepare()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player.release()
    }
}
