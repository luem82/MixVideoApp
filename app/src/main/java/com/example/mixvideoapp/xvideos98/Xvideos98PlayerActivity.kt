package com.example.mixvideoapp.xvideos98

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.allxinfo.DialoBrightnessFragment
import com.example.mixvideoapp.allxinfo.DialoVolumFragment
import com.example.mixvideoapp.databinding.ActivityXvideos98PlayerBinding
import com.example.mixvideoapp.databinding.CustomControllerXvideos98Binding
import com.example.mixvideoapp.databinding.CustomDialogFeatureBinding
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class Xvideos98PlayerActivity : AppCompatActivity(), Xvideos98Adapter.IXvideos98Listener {

    private val TAG = "Xvideos98PlayerActivity"
    private lateinit var binding: ActivityXvideos98PlayerBinding
    private lateinit var controllerBinding: CustomControllerXvideos98Binding
    private lateinit var xvideos98: Xvideos98
    private lateinit var listMore: ArrayList<Xvideos98>
    private lateinit var sourceUrl: String
    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var pictureInPictureParams: PictureInPictureParams.Builder
    private lateinit var playbackParameters: PlaybackParameters
    private var isCrossChecked = false
    private var isMute = false
    private var isSubtitle = true
    private var isNight = false
    private var isPortrait = false
    private var speed = 0f
    private val checkedItem = intArrayOf(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXvideos98PlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controllerBinding =
            CustomControllerXvideos98Binding.bind(findViewById(R.id.custom_controller_xvideo98))

        xvideos98 = intent.getSerializableExtra("Xvideos98") as Xvideos98
        listMore = intent.getSerializableExtra("listMore") as ArrayList<Xvideos98>
        loadData()
    }

    private fun loadData() {
        NetworkBasic.getRetrofit(XConsts.BASE_URL).create(XVideo98Api::class.java)
            .getVideoSource(xvideos98.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    sourceUrl = ParseXvideos98.parseSource(html)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "onError: ${e.message}")
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Lỗi kết nối truy cập. hãy chọn video khác",
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

                override fun onComplete() {
                    Log.e(TAG, "onComplete: ${sourceUrl}")
                    initPlayer()
                    setListeners()
                }
            })
    }

    private fun showMoreVideos() {
        val mAdapter = Xvideos98Adapter(R.layout.item_video_more, listMore, this)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvMoreVideos.setHasFixedSize(true)
        if (isPortrait) {
            binding.rvMoreVideos.layoutManager = GridLayoutManager(this, 2)
        } else {
            binding.rvMoreVideos.layoutManager = GridLayoutManager(this, 3)
        }
        binding.rvMoreVideos.adapter = mAdapter
    }

    @SuppressLint("Range")
    private fun setListeners() {

        controllerBinding.feSub.setOnClickListener {
            if (isSubtitle) {
                trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this)
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, true).build()
                Toast.makeText(this, "Đã bật phụ đề", Toast.LENGTH_SHORT).show()
                controllerBinding.feSub.imageTintList = ContextCompat.getColorStateList(
                    this, R.color.green
                )
                isSubtitle = false
            } else {
                trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this)
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, false).build()
                Toast.makeText(this, "Đã tắt phụ đề", Toast.LENGTH_SHORT).show()
                controllerBinding.feSub.imageTintList = ContextCompat.getColorStateList(
                    this, R.color.white
                )
                isSubtitle = true
            }
        }

        controllerBinding.feLock.setOnClickListener {
            controllerBinding.rootControl.setVisibility(View.INVISIBLE)
            controllerBinding.unLock.setVisibility(View.VISIBLE)
            Toast.makeText(this, "Đã khóa màn hình", Toast.LENGTH_SHORT).show()
        }

        controllerBinding.feNight.setOnClickListener {
            if (isNight) {
                binding.viewNight.setVisibility(View.GONE)
                isNight = false
            } else {
                binding.viewNight.setVisibility(View.VISIBLE)
                isNight = true
            }
        }

        controllerBinding.fePIP.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val aspectRatio = Rational(16, 9)
                pictureInPictureParams.setAspectRatio(aspectRatio)
                enterPictureInPictureMode(pictureInPictureParams.build())

                //onPictureInPictureModeChanged
                //onPause
                //onStop
            } else {
                Log.e(TAG, "pictureInPictureParams: Error")
                Toast.makeText(this, "Thiết bị không hỗ trợ PIP", Toast.LENGTH_SHORT).show()
            }
        }

        controllerBinding.feMute.setOnClickListener {
            if (isMute) {
                player?.setVolume(100f)
                controllerBinding.feMute.setImageResource(R.drawable.ic_baseline_music_off_24)
                isMute = false
                Toast.makeText(this, "Đã bật âm thanh", Toast.LENGTH_SHORT).show()
            } else {
                player?.setVolume(0f)
                controllerBinding.feMute.setImageResource(R.drawable.ic_baseline_music_on_24)
                isMute = true
                Toast.makeText(this, "Đã tắt âm thanh", Toast.LENGTH_SHORT).show()
            }
        }

        //brightness controler
        controllerBinding.feBright.setOnClickListener {
            val dialoBrightnessFragment = DialoBrightnessFragment()
            dialoBrightnessFragment.show(supportFragmentManager, "brightness")
        }

        //volum controler
        controllerBinding.feVol.setOnClickListener {
            val dialoVolumFragment = DialoVolumFragment()
            dialoVolumFragment.show(supportFragmentManager, "volum")
        }

        //quality
        controllerBinding.feQuality.setOnClickListener {
            initPopupQuality()
        }

        //show more video
        controllerBinding.feMore.setOnClickListener {
            if (binding.containerMoreVideos.visibility == View.GONE) {
                binding.containerMoreVideos.visibility = View.VISIBLE
            }
        }

        //speed
        controllerBinding.feSpeed.setOnClickListener {
            //            val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogCustom)
            val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogMaterialTheme)
            builder.setTitle("Tốc Độ Phát")
//            builder.background = ColorDrawable(Color.parseColor("#1c1c1c"))

            val items = arrayOf(
                "0.25x", "0.5x", "0.75x", "1x Tiêu chuẩn", "1.25x", "1.5x", "1.75x", "2.0x"
            )

            builder.setPositiveButton("OK", null)

            builder.setSingleChoiceItems(
                items, checkedItem.get(0)
            ) { dialog: DialogInterface?, which: Int ->
                when (which) {
                    0 -> speed = 0.25f
                    1 -> speed = 0.5f
                    2 -> speed = 0.75f
                    3 -> speed = 1.0f
                    4 -> speed = 1.25f
                    5 -> speed = 1.5f
                    6 -> speed = 1.75f
                    7 -> speed = 2.0f
                    else -> {
                    }
                }
                playbackParameters = PlaybackParameters(speed)
                player?.playbackParameters = playbackParameters
                checkedItem[0] = which
            }

            builder.create().show()
        }


        // more features dialog
        controllerBinding.featureVideo.setOnClickListener {
            val featureView = layoutInflater.inflate(
                R.layout.custom_dialog_feature, binding.root, false
            )
            val featureBinding = CustomDialogFeatureBinding.bind(featureView)
            val featureDialog = MaterialAlertDialogBuilder(this)
                .setView(featureView)
                .setBackground(ColorDrawable(Color.TRANSPARENT))
                .create()
            featureDialog.show()

            //lock screen
            featureBinding.btnLock.setOnClickListener { v ->
                featureDialog.dismiss()
                controllerBinding.rootControl.setVisibility(View.INVISIBLE)
                controllerBinding.unLock.setVisibility(View.VISIBLE)
                Toast.makeText(this, "Đã khóa màn hình", Toast.LENGTH_SHORT).show()
            }

            // volum on-off -> when dialog show
            if (isMute) {
                featureBinding.btnMute.setIconResource(R.drawable.ic_baseline_volume_up_24_green)
                featureBinding.btnMute.iconTint =
                    ContextCompat.getColorStateList(this, R.color.green)
                featureBinding.btnMute.setText("Mở âm thanh")
                featureBinding.btnMute.setTextColor(ContextCompat.getColor(this, R.color.green))
            } else {
                featureBinding.btnMute.setIconResource(R.drawable.ic_baseline_volume_off_24_red)
                featureBinding.btnMute.iconTint = ContextCompat.getColorStateList(this, R.color.red)
                featureBinding.btnMute.setText("Tắt âm thanh")
                featureBinding.btnMute.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
            featureBinding.btnMute.setOnClickListener { v ->
                featureDialog.dismiss()
                if (isMute) {
                    player?.setVolume(100f)
                    featureBinding.btnMute.setIconResource(R.drawable.ic_baseline_volume_off_24_red)
                    featureBinding.btnMute.iconTint =
                        ContextCompat.getColorStateList(this, R.color.red)
                    featureBinding.btnMute.setText("Tắt âm thanh")
                    featureBinding.btnMute.setTextColor(ContextCompat.getColor(this, R.color.red))
                    isMute = false
                    Toast.makeText(this, "Đã bật âm thanh", Toast.LENGTH_SHORT).show()
                } else {
                    player?.setVolume(0f)
                    featureBinding.btnMute.setIconResource(R.drawable.ic_baseline_volume_up_24_green)
                    featureBinding.btnMute.iconTint =
                        ContextCompat.getColorStateList(this, R.color.green)
                    featureBinding.btnMute.setText("Mở âm thanh")
                    featureBinding.btnMute.setTextColor(ContextCompat.getColor(this, R.color.green))
                    isMute = true
                    Toast.makeText(this, "Đã tắt âm thanh", Toast.LENGTH_SHORT).show()
                }
            }

            //quality
            featureBinding.btnQuality.setOnClickListener {
                featureDialog.dismiss()
                initPopupQuality()
            }

            //speed
            featureBinding.btnSpeed.setOnClickListener {
                featureDialog.dismiss()
                //            val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogCustom)
                val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogMaterialTheme)
                builder.setTitle("Tốc Độ Phát")
//            builder.background = ColorDrawable(Color.parseColor("#1c1c1c"))

                val items = arrayOf(
                    "0.25x", "0.5x", "0.75x", "1x Tiêu chuẩn", "1.25x", "1.5x", "1.75x", "2.0x"
                )

                builder.setPositiveButton("OK", null)

                builder.setSingleChoiceItems(
                    items, checkedItem.get(0)
                ) { dialog: DialogInterface?, which: Int ->
                    when (which) {
                        0 -> speed = 0.25f
                        1 -> speed = 0.5f
                        2 -> speed = 0.75f
                        3 -> speed = 1.0f
                        4 -> speed = 1.25f
                        5 -> speed = 1.5f
                        6 -> speed = 1.75f
                        7 -> speed = 2.0f
                        else -> {
                        }
                    }
                    playbackParameters = PlaybackParameters(speed)
                    player?.playbackParameters = playbackParameters
                    checkedItem[0] = which
                }

                builder.create().show()
            }

            //night mode
            featureBinding.btnNight.setOnClickListener {
                featureDialog.dismiss()
                if (isNight) {
                    binding.viewNight.setVisibility(View.GONE)
                    isNight = false
                } else {
                    binding.viewNight.setVisibility(View.VISIBLE)
                    isNight = true
                }
            }

            //brightness controler
            featureBinding.btnBrightness.setOnClickListener {
                featureDialog.dismiss()
                val dialoBrightnessFragment = DialoBrightnessFragment()
                dialoBrightnessFragment.show(supportFragmentManager, "brightness")
            }

            //volum controler
            featureBinding.btnSound.setOnClickListener {
                featureDialog.dismiss()
                val dialoVolumFragment = DialoVolumFragment()
                dialoVolumFragment.show(supportFragmentManager, "volum")
            }


            featureBinding.btnPip.setOnClickListener {
                featureDialog.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val aspectRatio = Rational(16, 9)
                    pictureInPictureParams.setAspectRatio(aspectRatio)
                    enterPictureInPictureMode(pictureInPictureParams.build())

                    //onPictureInPictureModeChanged
                    //onPause
                    //onStop
                } else {
                    Log.e(TAG, "pictureInPictureParams: Error")
                    Toast.makeText(this, "Thiết bị không hỗ trợ PIP", Toast.LENGTH_SHORT).show()
                }
            }
        }


        controllerBinding.unLock.setOnClickListener { v ->
            controllerBinding.rootControl.setVisibility(View.VISIBLE)
            controllerBinding.unLock.setVisibility(View.GONE)
            Toast.makeText(this, "Đã mở khóa màn hình", Toast.LENGTH_SHORT).show()
        }

        binding.closeMoreVideo.setOnClickListener {
            if (binding.containerMoreVideos.visibility == View.VISIBLE) {
                binding.containerMoreVideos.visibility = View.GONE
            }
        }

        //init pictureInPictureParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParams = PictureInPictureParams.Builder()
        }

        controllerBinding.backVideo.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initPlayer() {
        trackSelector = DefaultTrackSelector(/* context= */this, AdaptiveTrackSelection.Factory())
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector!!).build()
        doubleTapEnable()
//        binding.playerView.player = player

//        val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
//        val mediaItem = MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/chatsapp-9bf4e.appspot.com/o/tiktok%201.mp4?alt=media&token=2e6d2f35-dbeb-4161-bf78-f7923d2056e0")
//        val mediaItem = MediaItem.fromUri("https://zbporn.tv/get_file/26/a4a833b6a322ca3c3dd8f2d264760793af9e169b8c/603000/603263/603263.mp4/?br=1132&rnd=1675408645512")
//        val mediaItem = MediaItem.fromUri(sourceUrl)
//        val mediaSource = DefaultMediaSourceFactory(this).createMediaSource(mediaItem)

        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaItem = MediaItem.fromUri(sourceUrl)
        val mediaSource = HlsMediaSource.Factory(defaultHttpDataSourceFactory)
            .createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.addListener(object : Player.Listener {
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                val videoWidth = videoSize.width
                val videoHeight = videoSize.height
                Log.e(TAG, "onVideoSizeChanged: width: $videoWidth |||  height: $videoHeight")
                if (videoSize.width < videoSize.height) {
                    isPortrait = true
                    setFullscreen(true)
                } else {
                    isPortrait = false
                    setFullscreen(false)
                }
                showMoreVideos()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_READY) {
                    binding.progressBar.visibility = View.GONE
                } else if (playbackState == Player.STATE_ENDED) {
                    if (binding.containerMoreVideos.visibility == View.GONE) {
                        binding.containerMoreVideos.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "onPlayerError: ${error.message}")
                val snackbar = Snackbar.make(
                    binding.root,
                    "Video lỗi không tồn tại. hãy chọn video khác",
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
    }

    private fun doubleTapEnable() {
        binding.playerView.player = player
        binding.ytOverlay.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationStart() {
                binding.ytOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                binding.ytOverlay.visibility = View.GONE
            }
        })
        binding.ytOverlay.player(player!!)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Suppress("DEPRECATION")
    private fun setFullscreen(portrait: Boolean) {
        if (portrait) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        controllerBinding.titleVideo.text = xvideos98.title

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
    }

    override fun onClickXvideos98(nextVideo: Xvideos98) {
        val intent = intent
        intent.putExtra("Xvideos98", nextVideo)
        intent.putExtra("listMore", this.listMore)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (binding.containerMoreVideos.visibility == View.VISIBLE) {
            binding.containerMoreVideos.visibility = View.GONE
            return
        }
        if (player != null) {
            player?.release()
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
        player?.playWhenReady = false
        player?.playbackState
        //check pIP
        if (isInPictureInPictureMode) {
            player?.playWhenReady = true
        } else {
            player?.playWhenReady = false
            player?.playbackState
        }
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

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isCrossChecked = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            binding.playerView.hideController()
        } else {
            binding.playerView.showController()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
        if (isCrossChecked) {
            player?.release()
            finish()
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
//            getString(R.string.qualitySelector),
            "Chọn chất lượng video",
            trackSelector,
            videoRenderer
        )
        trackSelectionDialogBuilder.setTheme(R.style.AlertDialogMaterialTheme)
        trackSelectionDialogBuilder.setTrackNameProvider {
            // Override function getTrackName
            getString(R.string.exo_track_resolution_pixel, it.height)
        }

        val trackDialog = trackSelectionDialogBuilder.build()
        trackDialog.show()
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

}