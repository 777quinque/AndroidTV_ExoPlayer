package ip.tomichek.tv

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory

class FullScreenPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_player)

        // Скрываем ActionBar
        supportActionBar?.hide()
        // Убираем навигационное меню и статусбар
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        // Инициализация PlayerView и ExoPlayer
        playerView = findViewById(R.id.playerView)

        val renderersFactory = NextRenderersFactory(applicationContext).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
        }

        // Используем глобальную переменную player
        player = ExoPlayer.Builder(applicationContext)
            .setRenderersFactory(renderersFactory)
            .build()

        playerView.player = player

        // Устанавливаем полноэкранный режим
        setFullScreen()

        // Получаем URL видео из Intent
        val videoUrl = intent.getStringExtra("videoUrl")
        if (videoUrl != null) {
            // Создаем DataSource для загрузки контента
            val userAgent = "AndroidTV/9"
            val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)

            // Проверяем, если это HLS поток (m3u8)
            if (videoUrl.endsWith(".m3u8")) {
                // Создаем HLS MediaSource для воспроизведения
                val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))

                // Устанавливаем источник и подготавливаем плеер
                player.setMediaSource(hlsMediaSource)
            } else {
                // Если это не HLS, используем ProgressiveMediaSource
                val progressiveMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)))

                player.setMediaSource(progressiveMediaSource)
            }

            // Добавляем обработчики событий для отслеживания ошибок и состояния воспроизведения
            player.addListener(object : Player. Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        ExoPlayer.STATE_IDLE -> Log.d("ExoPlayer", "Idle state")
                        ExoPlayer.STATE_BUFFERING -> Log.d("ExoPlayer", "Buffering")
                        ExoPlayer.STATE_READY -> Log.d("ExoPlayer", "Ready to play")
                        ExoPlayer.STATE_ENDED -> Log.d("ExoPlayer", "Playback ended")
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer", "Error during playback", error)
                    Toast.makeText(this@FullScreenPlayerActivity, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
                }
            })

            // Подготавливаем плеер и начинаем воспроизведение
            player.prepare()
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы плеера
        player.release()
    }

    @OptIn(UnstableApi::class) private fun setFullScreen() {
        // Устанавливаем режим изменения размера для экрана
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT

        // Устанавливаем параметры ширины и высоты для полноэкранного режима
        val layoutParams = playerView.layoutParams as ViewGroup.LayoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        playerView.layoutParams = layoutParams

        // Скрываем панель навигации и статусбар для полноэкранного режима
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

