package com.example.mixvideoapp.videoquality

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityVideoQualityBinding
import com.example.mixvideoapp.databinding.CustomControllerViewBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util


class VideoQualityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoQualityBinding
    private lateinit var controllerBinding: CustomControllerViewBinding
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var trackSelector: DefaultTrackSelector
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true
    private var trackDialog: Dialog? = null
    private val mediaItem = MediaItem.Builder()
        .setUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
        .setMimeType(MimeTypes.APPLICATION_MP4)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoQualityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controllerBinding = CustomControllerViewBinding.bind(findViewById(R.id.custom_controller))

        dataSourceFactory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "testapp")
        )

        controllerBinding.fullScreen.setOnClickListener {
            if (trackDialog == null) initPopupQuality()
            trackDialog?.show()
        }

        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
    }

    private fun initPlayer() {

        trackSelector = DefaultTrackSelector(this)
        // When player is initialized it'll be played with a quality of MaxVideoSize to prevent loading in 1080p from the start
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setMaxVideoSize(MAX_WIDTH, MAX_HEIGHT)
        )
        exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem)
            prepare()
        }
        binding.playerView.player = exoPlayer

        //Listener on player
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
//                    exoQuality.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun releasePlayer() {
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentWindowIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentWindowIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initPlayer()
            binding.playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            initPlayer()
            binding.playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            binding.playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.playerView.onPause()
            releasePlayer()
        }
    }

    // QUALITY SELECTOR
    private fun initPopupQuality() {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        var videoRenderer: Int? = null

        if (mappedTrackInfo == null) return

        for (i in 0 until mappedTrackInfo.rendererCount) {
            if (isVideoRenderer(mappedTrackInfo, i)) videoRenderer = i
        }

        if (videoRenderer == null) {
            return
        }

        val trackSelectionDialogBuilder = TrackSelectionDialogBuilder(
            this,
            getString(R.string.qualitySelector),
            trackSelector,
            videoRenderer
        )
        trackSelectionDialogBuilder.setTheme(R.style.AlertDialogMaterialTheme)
        trackSelectionDialogBuilder.setTrackNameProvider {
            // Override function getTrackName
            getString(R.string.exo_track_resolution_pixel, it.height)
        }
        trackDialog = trackSelectionDialogBuilder.build()
    }

    private fun isVideoRenderer(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
        rendererIndex: Int
    ): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) return false
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_VIDEO == trackType
    }

    companion object {
        const val HLS_STATIC_URL =
            "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
        const val MAX_HEIGHT = 539
        const val MAX_WIDTH = 959
    }
}