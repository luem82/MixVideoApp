package com.example.mixvideoapp.pornogids

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.allxinfo.DialoBrightnessFragment
import com.example.mixvideoapp.allxinfo.DialoVolumFragment
import com.example.mixvideoapp.databinding.ActivityGidsPlayerBinding
import com.example.mixvideoapp.databinding.CustomControllerGidsBinding
import com.example.mixvideoapp.databinding.CustomDialogFeatureBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class GidsPlayerActivity : AppCompatActivity(), GidsVideoAdapter.IGidsVideoListener {

    private val TAG = "GidsPlayerActivity"
    private lateinit var binding: ActivityGidsPlayerBinding
    private lateinit var controllerBinding: CustomControllerGidsBinding
    private lateinit var gidsVideo: GidsVideo
    private lateinit var detailGidsVideo: DetailGidsVideo
    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var pictureInPictureParams: PictureInPictureParams.Builder
    private var isFullscreen = false
    private var isCrossChecked = false
    private var isPortrait = false
    private var isMute = false
    private var isNight = false
    private lateinit var playbackParameters: PlaybackParameters
    private var speed = 0f
    private val checkedItem = intArrayOf(3)

//lock, mute, night, pip, volum, brightness, quality, speed,

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGidsPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        controllerBinding =
            CustomControllerGidsBinding.bind(findViewById(R.id.custom_controller_gids))

        gidsVideo = intent.getSerializableExtra("GidsVideo") as GidsVideo
        getVideoDetail()
    }

    private fun getVideoDetail() {
        NetworkBasic.getRetrofit("https://pornogids.net/").create(GidsApi::class.java)
            .getDetailVideo(gidsVideo.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    detailGidsVideo = ParseGids.parseDetailVideo(html)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(
                        this@GidsPlayerActivity,
                        "Video lỗi, hãy thử chọn video khác.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onComplete() {
                    Log.e(TAG, "onComplete: ${detailGidsVideo}")
                    displayVideDetail()
                    initPlayer()
                }
            })
    }

    private fun displayVideDetail() {
        binding.tvDate.text =
            "${detailGidsVideo.date} | Thời lượng: ${gidsVideo.duration}\nLượt xem: ${gidsVideo.views} | Bình chọn: ${gidsVideo.rates}"

        val translateTitle = TranslateAPI(
            Language.AUTO_DETECT, Language.VIETNAMESE, gidsVideo.title
        )
        translateTitle.setTranslateListener(object : TranslateAPI.TranslateListener {
            override fun onSuccess(translatedText: String?) {
                binding.tvTitle.text = translatedText
                controllerBinding.titleVideo.text = translatedText
            }

            override fun onFailure(ErrorText: String?) {
                binding.tvTitle.text = gidsVideo.title
                controllerBinding.titleVideo.text = gidsVideo.title
            }

        })

        if (detailGidsVideo.equals("Video không có mô tả")) {
            binding.tvDesc.text = detailGidsVideo.desciption
        } else {
            val translateDesc = TranslateAPI(
                Language.AUTO_DETECT, Language.VIETNAMESE, detailGidsVideo.desciption
            )
            translateDesc.setTranslateListener(object : TranslateAPI.TranslateListener {
                override fun onSuccess(translatedText: String?) {
                    binding.tvDesc.text = translatedText
                }

                override fun onFailure(ErrorText: String?) {
                    binding.tvDesc.text = detailGidsVideo.desciption
                }

            })
        }

        val gidsVideoAdapter = GidsVideoAdapter(
            R.layout.item_video_gids, detailGidsVideo.listRelated, this
        )
        gidsVideoAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = gidsVideoAdapter
        gidsVideoAdapter.data.removeAt(0)
        gidsVideoAdapter.notifyDataSetChanged()
        binding.tvLbl1.visibility = View.VISIBLE
    }

    override fun onGidsVideoClick(gidsVideo: GidsVideo) {
        if (binding.tvLbl1.visibility == View.VISIBLE) {
            val intent = intent
            intent.putExtra("GidsVideo", gidsVideo)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            finish()
            startActivity(intent)
        }
    }

    private fun initPlayer() {
        if (isFullscreen) {
            controllerBinding.titleVideo.visibility = View.VISIBLE
        } else {
            controllerBinding.titleVideo.visibility = View.GONE
        }
        trackSelector = DefaultTrackSelector(/* context= */this, AdaptiveTrackSelection.Factory())
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector!!).build()
        binding.playerView.player = player

        val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
//        val mediaItem = MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4")
//        val mediaItem = MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/chatsapp-9bf4e.appspot.com/o/tiktok%201.mp4?alt=media&token=2e6d2f35-dbeb-4161-bf78-f7923d2056e0")
//        val mediaItem = MediaItem.fromUri("https://zbporn.tv/get_file/26/a4a833b6a322ca3c3dd8f2d264760793af9e169b8c/603000/603263/603263.mp4/?br=1132&rnd=1675408645512")
        val mediaItem = MediaItem.fromUri(detailGidsVideo.source)
        val mediaSource = DefaultMediaSourceFactory(this).createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                val videoWidth = videoSize.width
                val videoHeight = videoSize.height
                Log.e(TAG, "onVideoSizeChanged: width: $videoWidth |||  height: $videoHeight")
                if (videoSize.width < videoSize.height) {
                    isPortrait = true
                }
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
                Toast.makeText(
                    this@GidsPlayerActivity,
                    "Video lỗi, hãy thử chọn video khác.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        player?.prepare()
        player?.playWhenReady = true
        setListeners()
    }

    @SuppressLint("Range")
    private fun setListeners() {

        //PictureInPictureParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParams = PictureInPictureParams.Builder()
        }

        //more features
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
                controllerBinding.fullScreen.performClick()
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
                if (isFullscreen) {
                    if (isNight) {
                        binding.viewNight.setVisibility(View.GONE)
                        isNight = false
                    } else {
                        binding.viewNight.setVisibility(View.VISIBLE)
                        isNight = true
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Tính năng này chỉ thực hiện ở chế độ toàn màn hình.",
                        Toast.LENGTH_LONG
                    ).show()
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

        controllerBinding.fullScreen.setOnClickListener {
            if (isFullscreen) {
                isFullscreen = false
                setFullscreen(isFullscreen)
            } else {
                isFullscreen = true
                setFullscreen(isFullscreen)
            }
        }

        controllerBinding.backVideo.setOnClickListener { onBackPressed() }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            controllerBinding.titleVideo.visibility = View.VISIBLE
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_zoom_out)
            if (isPortrait) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            setSystemUIVisibility(fullscreen)
            val params = binding.playerView.layoutParams as ConstraintLayout.LayoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.playerView.layoutParams = params
        } else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            controllerBinding.titleVideo.visibility = View.INVISIBLE
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_zoom_in)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setSystemUIVisibility(fullscreen)
            val params = binding.playerView.layoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.height = 0
            binding.playerView.layoutParams = params
        }
    }

    @Suppress("DEPRECATION")
    private fun setSystemUIVisibility(hide: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val window = window.insetsController!!
            val windows = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()

            if (hide) window.hide(windows) else window.show(windows)

            // needed for hide, doesn't do anything in show
            window.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val view = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            window.decorView.systemUiVisibility = if (hide) view else view.inv()
        }
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            isFullscreen = false
            setFullscreen(isFullscreen)
            return
        } else {
            if (player != null) {
                player?.release()
            }
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
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