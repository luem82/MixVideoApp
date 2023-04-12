package com.example.mixvideoapp.videoquality

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.mixvideoapp.databinding.ActivityKotlinExtenBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.TracksInfo
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverrides
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util

class KotlinExtenActivity : AppCompatActivity(), Player.Listener {

    private lateinit var binding: ActivityKotlinExtenBinding
    private var qualityPopUp: PopupMenu? = null
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var trackSelector: DefaultTrackSelector? = null
    var qualityList = ArrayList<Pair<String, TrackSelectionOverrides.Builder>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKotlinExtenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnQuality.setOnClickListener {
            qualityPopUp?.show()
        }
    }


    private fun initPlayer() {
        trackSelector = DefaultTrackSelector(/* context= */this, AdaptiveTrackSelection.Factory())
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector!!).build()
        player?.playWhenReady = true
        binding.playerView.player = player
        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()

        val mediaItem =
            MediaItem.fromUri("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8")
        val mediaSource =
            HlsMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)

//        val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
//        val mediaSource = DefaultMediaSourceFactory(this).createMediaSource(mediaItem)


        player?.setMediaSource(mediaSource)
        player?.seekTo(playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.addListener(this)
        player?.prepare()

    }

    override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
        super.onTrackSelectionParametersChanged(parameters)
        Log.e("TAG", "maxVideoBitrate: ${parameters.maxVideoBitrate}")
        Log.e("TAG", "minVideoBitrate: ${parameters.minVideoBitrate}")
        Log.e("TAG", "maxVideoWidth: ${parameters.maxVideoWidth}")
        Log.e("TAG", "minVideoHeight: ${parameters.minVideoHeight}")
    }

    private fun setUpQualityList() {
        qualityPopUp = PopupMenu(this, binding.btnQuality)
        qualityList.let {
            for ((i, videoQuality) in it.withIndex()) {
                qualityPopUp?.menu?.add(0, i, 0, videoQuality.first)
            }
        }
        qualityPopUp?.setOnMenuItemClickListener { menuItem ->
            qualityList[menuItem.itemId].let {
                trackSelector!!.setParameters(
                    trackSelector!!.getParameters()
                        .buildUpon()
                        .setTrackSelectionOverrides(it.second.build())
                        .setTunnelingEnabled(true)
                        .build()
                )
            }
            true
        }
    }

    override fun onTracksInfoChanged(tracksInfo: TracksInfo) {
        println("TRACK CHANGED")
        println(tracksInfo.trackGroupInfos)
        Log.i("TAG", "minVideoHeight: ${tracksInfo.trackGroupInfos}")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            trackSelector?.generateQualityList()?.let {
                qualityList = it
                setUpQualityList()
            }
            binding.progressBar.visibility = View.GONE
        } else if (playbackState == Player.STATE_BUFFERING) {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            it.release()
            player = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initPlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
}