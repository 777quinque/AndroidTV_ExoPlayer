package ip.tomichek.tv

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IVLCVout
import org.videolan.libvlc.media.VideoView
import java.util.concurrent.TimeUnit

private lateinit var btnPlayPause: ImageButton
private lateinit var seekBar: SeekBar
private lateinit var tvDuration: TextView
private lateinit var tvCurrentTime: TextView



class FullScreenPlayerActivity : AppCompatActivity(), IVLCVout.Callback {
    private val handler = Handler()
    private val hideControlsRunnable = Runnable {
        hideControls()
    }

    private lateinit var videoView: VideoView
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_player)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        videoView = findViewById(R.id.fullScreenPlayerView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        seekBar = findViewById(R.id.seekBar)
        tvDuration = findViewById(R.id.tvDuration)
        tvCurrentTime = findViewById(R.id.tvCurrentTime)

        val videoUrl = intent.getStringExtra("videoUrl")
        if (videoUrl != null) {
            libVLC = LibVLC(this)
            mediaPlayer = MediaPlayer(libVLC)

            val userAgent = "AndroidTV/9"
            val media = Media(libVLC, Uri.parse(videoUrl))
            media.addOption(":http-user-agent=$userAgent")
            mediaPlayer.media = media

            setupControls()

            setFullScreen()

            videoView.setOnTouchListener { _, event ->
                // Обработка нажатия на экран для отображения элементов управления
                showControls()
                true
            }
            // Настройка вывода для SurfaceView
            val vlcVout: IVLCVout = mediaPlayer.vlcVout
            vlcVout.setVideoView(videoView)
            vlcVout.addCallback(this)
            vlcVout.attachViews()

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            progressBar = findViewById(R.id.progressBar2)
            mediaPlayer.aspectRatio = "16:9"

            mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Buffering -> {
                        // Обработка события буферизации
                        if (event.buffering >= 0 && event.buffering < 100) {
                            // Видео буфируется
                            progressBar.visibility = View.VISIBLE
                        } else {
                            // Воспроизведение возобновлено
                            progressBar.visibility = View.GONE
                        }
                    }
                    MediaPlayer.Event.EncounteredError -> {
                        // Обработка ошибок воспроизведения
                    }
                    MediaPlayer.Event.Vout -> {

                        if (event.voutCount > 0) {
                            vlcVout.setWindowSize(videoView.width, videoView.height)
                            mediaPlayer.play()
                        }
                    }
                    MediaPlayer.Event.Playing -> {

                        startControlsTimer()

                        // Обновляем длительность и seekbar
                        updateDurationAndSeekBar()
                    }
                    MediaPlayer.Event.PositionChanged -> {
                        // Обработка изменения позиции (перемотка)
                        updateDurationAndSeekBar()
                    }
                    MediaPlayer.Event.LengthChanged -> {
                        // Обновление длительности видео
                        val durationInSeconds = mediaPlayer.length / 1000
                        val durationString = String.format(
                            "%02d:%02d:%02d",
                            TimeUnit.SECONDS.toHours(durationInSeconds),
                            TimeUnit.SECONDS.toMinutes(durationInSeconds) % 60,
                            durationInSeconds % 60
                        )
                        tvDuration.text = durationString
                    }
                    MediaPlayer.Event.TimeChanged -> {

                        val currentTimeInSeconds = mediaPlayer.time / 1000
                        val currentTimeString = String.format(
                            "%02d:%02d:%02d",
                            TimeUnit.SECONDS.toHours(currentTimeInSeconds),
                            TimeUnit.SECONDS.toMinutes(currentTimeInSeconds) % 60,
                            currentTimeInSeconds % 60
                        )
                        tvCurrentTime.text = currentTimeString


                        seekBar.progress = currentTimeInSeconds.toInt()
                    }

                }
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                    if (fromUser) {

                        if (mediaPlayer.isSeekable) {

                            val newPosition = progress * mediaPlayer.length / (seekBar?.max ?: 1)
                            mediaPlayer.time = newPosition
                        } else {

                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    showControls()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Пользователь закончил трекинг, сбрасываем таймер
                    resetControlsTimer()

                }
            })
            seekBar.setOnKeyListener { _, keyCode, event ->
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            showControls() // Отображение элементов управления
                            resetControlsTimer() // Сброс таймера
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if (event.action == KeyEvent.ACTION_DOWN) {
                            // Обработка нажатия кнопки вправо
                            showControls() // Отображение элементов управления
                            resetControlsTimer() // Сброс таймера
                        }
                        true
                    }
                    else -> false
                }
            }
            mediaPlayer.play()
        }
    }

    private fun setupControls() {
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)

        btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                mediaPlayer.play()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

    }
    private fun updateDurationAndSeekBar() {
        // Обновление длительности видео
        val durationInSeconds = mediaPlayer.length / 1000
        val durationString = String.format(
            "%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(durationInSeconds),
            TimeUnit.SECONDS.toMinutes(durationInSeconds) % 60,
            durationInSeconds % 60
        )
        tvDuration.text = durationString

        // Обновление положения полосы прокрутки
        seekBar.max = durationInSeconds.toInt()
    }

    private fun startControlsTimer() {

        handler.removeCallbacks(hideControlsRunnable)

        // Запланировать скрытие через 4 секунды в паузе
        handler.postDelayed(hideControlsRunnable, 4000)
    }

    private fun hideControls() {
        btnPlayPause.visibility = View.INVISIBLE
        seekBar.visibility = View.INVISIBLE
        tvDuration.visibility = View.INVISIBLE
        tvCurrentTime.visibility = View.INVISIBLE
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showControls() {
        btnPlayPause.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
        tvDuration.visibility = View.VISIBLE
        tvCurrentTime.visibility = View.VISIBLE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        resetControlsTimer()
    }

    private fun resetControlsTimer() {

        handler.removeCallbacks(hideControlsRunnable)

        // Запланировать скрытие через 4 секунды в паузе
        handler.postDelayed(hideControlsRunnable, 4000)
    }

    override fun onSurfacesCreated(vlcVout: IVLCVout) {

    }

    override fun onSurfacesDestroyed(vlcVout: IVLCVout) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.release()
        libVLC.release()
    }

    private fun setFullScreen() {

        val vlcVout: IVLCVout = mediaPlayer.vlcVout
        vlcVout.setWindowSize(videoView.width, videoView.height)
    }
}