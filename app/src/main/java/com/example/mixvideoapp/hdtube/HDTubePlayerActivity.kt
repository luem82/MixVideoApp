package com.example.mixvideoapp.hdtube

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityHdtubePlayerBinding
import com.example.mixvideoapp.databinding.CustomControllerGidsBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.snackbar.Snackbar

@Suppress("DEPRECATION")
class HDTubePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHdtubePlayerBinding
    private lateinit var controllerBinding: CustomControllerGidsBinding
    private lateinit var title: String
    private lateinit var source: String
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHdtubePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controllerBinding = CustomControllerGidsBinding.bind(
            findViewById(R.id.custom_controller_gids)
        )
        title = intent.getStringExtra("title").toString()
        source = intent.getStringExtra("source").toString()
        initPlayer()
    }

    private fun initPlayer() {
        controllerBinding.featureVideo.visibility = View.INVISIBLE
        controllerBinding.fullScreen.visibility = View.INVISIBLE
        controllerBinding.titleVideo.text = title
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
//        val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
//        val mediaItem = MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/chatsapp-9bf4e.appspot.com/o/tiktok%201.mp4?alt=media&token=2e6d2f35-dbeb-4161-bf78-f7923d2056e0")
//        val mediaItem = MediaItem.fromUri("https://zbporn.tv/get_file/26/a4a833b6a322ca3c3dd8f2d264760793af9e169b8c/603000/603263/603263.mp4/?br=1132&rnd=1675408645512")
        val mediaItem = MediaItem.fromUri(source)
        val mediaSource = DefaultMediaSourceFactory(this).createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    binding.progressBar.visibility = View.GONE
                } else if (playbackState == Player.STATE_BUFFERING) {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                val snackbar = Snackbar.make(
                    binding.root,
                    "Lỗi trình phát video.",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setTextColor(Color.parseColor("#ffffff"))
                snackbar.setActionTextColor(Color.parseColor("#ffa500"))
                snackbar.setAction("Quay về") {
                    snackbar.dismiss()
                    onBackPressed()
                }
                snackbar.show()
            }
        })
        player?.prepare()
        player?.playWhenReady = true

        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val window = window.insetsController!!
            val windows = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            window.hide(windows)
            // needed for hide, doesn't do anything in show
            window.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val view = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window.decorView.systemUiVisibility = view
        }

        controllerBinding.backVideo.setOnClickListener { onBackPressed() }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        Log.e("TAG", "onPause: ")
        player?.playWhenReady = false
        player?.playbackState
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
        player?.playbackState
    }

    override fun onRestart() {
        super.onRestart()
        player?.playWhenReady = true
        player?.playbackState
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.release()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("TAG", "onStop: ")
        player?.release()
    }
}