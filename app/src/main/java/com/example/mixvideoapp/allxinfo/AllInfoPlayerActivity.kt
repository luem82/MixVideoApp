package com.example.mixvideoapp.allxinfo

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.bullhead.equalizer.DialogEqualizerFragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityAllInfoPlayerBinding
import com.example.mixvideoapp.databinding.ExoControllerNewBinding
import com.example.mixvideoapp.databinding.SwipeZoomDesignBinding
import com.example.mixvideoapp.util.Consts
import com.github.vkay94.dtpv.youtube.YouTubeOverlay
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers


class AllInfoPlayerActivity : AppCompatActivity(), AllXVideoAdapter.IVideoListener {

    private val TAG = "AllInfoPlayerActivity"
    private lateinit var mainBinding: ActivityAllInfoPlayerBinding
    private lateinit var controllerBinding: ExoControllerNewBinding
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var playbackParameters: PlaybackParameters
    private lateinit var infoDetail: InfoDetail
    private var isFullScreen = false
    private var isCrossChecked = false
    private lateinit var pictureInPictureParams: PictureInPictureParams.Builder
    private var speed = 0f
    private val checkedItem = intArrayOf(3)

    //swipe
    private lateinit var swipeZoomBinding: SwipeZoomDesignBinding
    private var device_height = 0
    private var device_width: Int = 0
    private var brightness: Int = 0
    private var media_volum: Int = 0
    private var start = false
    private var singleTap = false
    private var left = false
    private var right = false
    private var base_x = 0f
    private var base_y = 0f
    private var swipe_move = false
    private var diff_x = 0L
    private var diff_y = 0L
    private var success = false
    private var audioManager: AudioManager? = null
    private val MINIMUM_DISTANCE = 100

    private var brightnessActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // There are no request codes
            val data = result.data
            //                            doSomeOperations();
            val value: Boolean
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                value = Settings.System.canWrite(applicationContext)
                if (value) {
                    success = true
                } else {
                    Toast.makeText(this, "Not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityAllInfoPlayerBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        controllerBinding = ExoControllerNewBinding.bind(findViewById(R.id.exo_controller_new))
        swipeZoomBinding = SwipeZoomDesignBinding.bind(findViewById(R.id.swipe_zom_container))
        hideSystemUI()

        infoDetail = intent.getSerializableExtra("infoDetail") as InfoDetail

        playVideo(infoDetail.videoSource)
        setListeners()
        setSwipe()
    }

    @Suppress("deprecation")
    @SuppressLint("ClickableViewAccessibility")
    private fun setSwipe() {

        val contentResolver = contentResolver
        val window = window
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        device_width = displayMetrics.widthPixels
        device_height = displayMetrics.heightPixels

        mainBinding.playerView.setOnTouchListener(object : OnSwipeTouchListener(this) {

            override fun onTouch(v: View, motionEvent: MotionEvent): Boolean {

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
//                        mainBinding.playerView.showController()
                        start = true
                        if (motionEvent.x < device_width / 2) {
                            left = true
                            right = false
                        } else if (motionEvent.x > device_width / 2) {
                            left = false
                            right = true
                        }
                        base_x = motionEvent.x
                        base_y = motionEvent.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        swipe_move = true
                        diff_x = Math.ceil((motionEvent.x - base_x).toDouble()).toLong()
                        diff_y = Math.ceil((motionEvent.y - base_y).toDouble()).toLong()
                        val brightnessSpeed = 0.01
                        if (Math.abs(diff_y) > MINIMUM_DISTANCE) {
                            mainBinding.playerView.showController()
                            start = true
                            if (Math.abs(diff_y) > Math.abs(diff_x)) {
                                val value: Boolean
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    value = Settings.System.canWrite(applicationContext)
                                    if (value) {
                                        if (left) {
                                            // brightness swipe
                                            Log.e(TAG, "onTouch: swipe left")
                                            try {
                                                Settings.System.putInt(
                                                    contentResolver,
                                                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                                                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                                                )
                                                brightness = Settings.System.getInt(
                                                    contentResolver,
                                                    Settings.System.SCREEN_BRIGHTNESS
                                                )
                                            } catch (e: SettingNotFoundException) {
                                                e.printStackTrace()
                                            }
                                            var new_brightness =
                                                (brightness - diff_y * brightnessSpeed).toInt()
                                            if (new_brightness > 250) {
                                                new_brightness = 250
                                            } else if (new_brightness < 1) {
                                                new_brightness = 1
                                            }
                                            val brt_percentage =
                                                Math.ceil(new_brightness.toDouble() / 250.toDouble() * 100.toDouble())
                                            swipeZoomBinding.brtProgressContainer.visibility =
                                                View.VISIBLE
                                            swipeZoomBinding.tvBrt.visibility = View.VISIBLE
                                            swipeZoomBinding.brtProgress.progress =
                                                brt_percentage.toInt()
                                            swipeZoomBinding.tvBrt.text =
                                                "Brightness: " + brt_percentage.toInt() + " %"

                                            if (brt_percentage < 1) {
                                                swipeZoomBinding.ivBrt.setImageResource(R.drawable.ic_baseline_brightness_low_24)
                                            } else if (brt_percentage > 1 && brt_percentage < 51) {
                                                swipeZoomBinding.ivBrt.setImageResource(R.drawable.ic_baseline_brightness_medium_24)
                                            } else {
                                                swipeZoomBinding.ivBrt.setImageResource(R.drawable.ic_baseline_brightness_high_24)
                                            }

                                            Log.e("onTouchBrightness", "new: ${new_brightness}")
                                            Log.e("onTouchBrightness", "percent: ${brt_percentage}")
                                            Log.e("onTouchBrightness", "old: ${brightness}")

                                            Settings.System.putInt(
                                                contentResolver,
                                                Settings.System.SCREEN_BRIGHTNESS, new_brightness
                                            )
                                            val layoutParams = window.attributes
                                            layoutParams.screenBrightness =
                                                brightness / 250.toFloat()
                                            window.attributes = layoutParams
                                        } else if (right) {
                                            // volum swipe
                                            Log.e(TAG, "onTouch: swipe right")
                                            swipeZoomBinding.volProgressContainer.visibility =
                                                View.VISIBLE
                                            swipeZoomBinding.tvVol.visibility = View.VISIBLE
                                            media_volum =
                                                audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            val max_volum =
                                                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                            val cal =
                                                diff_y.toDouble() * (max_volum.toDouble() / ((device_height * 2).toDouble() - brightnessSpeed))
                                            var new_media_volum = media_volum - cal.toInt()
                                            if (new_media_volum > max_volum) {
                                                new_media_volum = media_volum
                                            } else if (new_media_volum < 1) {
                                                new_media_volum = 0
                                            }
                                            audioManager!!.setStreamVolume(
                                                AudioManager.STREAM_MUSIC,
                                                new_media_volum,
                                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                                            )
                                            val volum_percent =
                                                Math.ceil(new_media_volum.toDouble() / max_volum.toDouble() * 100.toDouble())
                                            swipeZoomBinding.volProgress.progress =
                                                volum_percent.toInt()
                                            swipeZoomBinding.tvVol.text =
                                                "Volum: " + volum_percent.toInt() + " %"
                                            if (volum_percent < 1) {
                                                swipeZoomBinding.ivVol.setImageResource(R.drawable.ic_baseline_volume_off_24)
                                            } else if (volum_percent > 1 && volum_percent < 51) {
                                                swipeZoomBinding.ivVol.setImageResource(R.drawable.ic_baseline_volume_down_24)
                                            } else {
                                                swipeZoomBinding.ivVol.setImageResource(R.drawable.ic_baseline_volume_up_24)
                                            }
                                            Log.e(TAG, "onTouchVolum: ${volum_percent}")
                                        }
                                        success = true
                                    } else {
                                        Toast.makeText(
                                            this@AllInfoPlayerActivity,
                                            "Bật cài đặt ghi để kiểm soát độ sáng",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                        intent.data = Uri.parse("package:$packageName")
                                        brightnessActivityResultLauncher.launch(intent)
                                    }
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        swipe_move = false
                        start = false
                        swipeZoomBinding.volProgressContainer.visibility = View.GONE
                        swipeZoomBinding.tvVol.visibility = View.GONE
                        swipeZoomBinding.brtProgressContainer.visibility = View.GONE
                        swipeZoomBinding.tvBrt.visibility = View.GONE
                    }
                }


                return super.onTouch(v, motionEvent)
            }

            override fun onDoubleTouch() {
                super.onDoubleTouch()
            }

            override fun onSingleTouch() {
                super.onSingleTouch()
//                if (singleTap) {
//                    mainBinding.playerView.showController();
//                    singleTap = false;
//                } else {
//                    mainBinding.playerView.hideController();
//                    singleTap = true;
//                }
            }
        })
    }


    private fun setListeners() {

        //more options
        controllerBinding.ibtnMore.setOnClickListener {

            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.menu_video_more, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.video_add_favorite -> {
                        Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.video_download -> {
                        Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.video_info_detail -> {
                        Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.video_related -> {
                        if (mainBinding.contMoreVideo.visibility == View.GONE) {
                            loadRelatedVideos()
                            mainBinding.contMoreVideo.visibility = View.VISIBLE
                            if (simpleExoPlayer.isPlaying) {
                                simpleExoPlayer.pause()
                            }
                        }
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        //speed controler
        controllerBinding.ibtnSpeed.setOnClickListener {
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
                simpleExoPlayer.playbackParameters = playbackParameters
                checkedItem[0] = which
            }

            builder.create().show()
        }


        //brightness controler
        controllerBinding.ibtnBright.setOnClickListener {
            val dialoBrightnessFragment = DialoBrightnessFragment()
            dialoBrightnessFragment.show(supportFragmentManager, "brightness")
        }

        //volum controler
        controllerBinding.ibtnVolum.setOnClickListener {
            val dialoVolumFragment = DialoVolumFragment()
            dialoVolumFragment.show(supportFragmentManager, "volum")
        }

        //PictureInPictureParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParams = PictureInPictureParams.Builder()
        }
        controllerBinding.ibtnPip.setOnClickListener {
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

        mainBinding.ivClose.setOnClickListener {
            if (mainBinding.contMoreVideo.visibility == View.VISIBLE) {
                mainBinding.contMoreVideo.visibility = View.GONE
            }
        }

        controllerBinding.ibtnEqualizer.setOnClickListener {
            val sessionId = simpleExoPlayer.audioSessionId
            //equalize dialog
            val fragment = DialogEqualizerFragment.newBuilder()
                .setAudioSessionId(sessionId)
                .themeColor(ContextCompat.getColor(this, R.color.purple_700))
                .textColor(ContextCompat.getColor(this, R.color.white))
                .accentAlpha(ContextCompat.getColor(this, R.color.teal_700))
                .darkColor(ContextCompat.getColor(this, R.color.teal_700))
                .setAccentColor(ContextCompat.getColor(this, R.color.teal_700))
                .build()
            fragment.show(supportFragmentManager, "eq")
        }

        controllerBinding.ibtnLock.setOnClickListener {
            controllerBinding.rootCont.visibility = View.INVISIBLE
            controllerBinding.ivUnlock.visibility = View.VISIBLE
            Toast.makeText(this, "Đã khóa màn hình", Toast.LENGTH_SHORT).show()
        }

        controllerBinding.ivUnlock.setOnClickListener {
            controllerBinding.rootCont.visibility = View.VISIBLE
            controllerBinding.ivUnlock.visibility = View.GONE
            Toast.makeText(this, "Đã mở khóa màn hình", Toast.LENGTH_SHORT).show()
        }

        controllerBinding.ibtnScale.setOnClickListener {
            if (isFullScreen) {
                isFullScreen = false
                setFullScreen(isFullScreen)
            } else {
                isFullScreen = true
                setFullScreen(isFullScreen)
            }
        }

        controllerBinding.ibtnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setFullScreen(fullScreen: Boolean) {
        if (fullScreen) {
            mainBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            simpleExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            controllerBinding.ibtnScale.setImageResource(R.drawable.ic_zoom_out)
        } else {
            mainBinding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            simpleExoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            controllerBinding.ibtnScale.setImageResource(R.drawable.ic_zoom_in)
        }
    }

    @Suppress("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    private fun hideSystemUI() {
        //call before setContentView
//        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
//            val v = this.window.decorView
//            v.systemUiVisibility = View.GONE
//        } else if (Build.VERSION.SDK_INT >= 19) {
//            //for new api versions.
//            val decorView = window.decorView
//            val uiOptions =
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            decorView.systemUiVisibility = uiOptions
//        }

        // call after setContentView
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

    private fun loadRelatedVideos() {
        val mAdapter = AllXVideoAdapter(R.layout.item_video_allxinfo, infoDetail.listRelated, this)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        mainBinding.rvVideo.setHasFixedSize(true)
        mainBinding.rvVideo.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)

        mainBinding.contMoreVideo.visibility = View.VISIBLE

        mainBinding.ivClose.setOnClickListener {
            mainBinding.contMoreVideo.visibility = View.GONE
        }
    }

    private fun playVideo(url: String) {
        controllerBinding.tvTitle.text = infoDetail.videoTitle
        val uri = Uri.parse(url)
//        val uri = Uri.parse("https://html5videoformatconverter.com/data/images/happyfit2.mp4")
        val mediaItem = MediaItem.fromUri(uri)
        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        doubleTapEnable()
        seekBarFeature()
//        simpleExoPlayer.playbackParameters = playbackParameters
        simpleExoPlayer.setMediaItem(mediaItem)
        simpleExoPlayer.prepare()
        simpleExoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    controllerBinding.pbLoading.visibility = View.VISIBLE
                } else if (playbackState == Player.STATE_READY) {
                    controllerBinding.pbLoading.visibility = View.GONE
                } else if (playbackState == Player.STATE_ENDED) {
                    loadRelatedVideos()
                }
            }

            @SuppressLint("SourceLockedOrientationActivity")
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val videoWidth = videoSize.width
                val videoHeight = videoSize.height
                Log.e(TAG, "getVideoFrame: width: $videoWidth |||  height: $videoHeight")
                if (videoSize.width > videoSize.height) {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val snackbar = Snackbar.make(
                    mainBinding.root,
                    "Lỗi video không tồn tại.",
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setTextColor(Color.parseColor("#ffffff"))
                snackbar.setActionTextColor(Color.parseColor("#ffa500"))
                snackbar.setAction("Quay về") {
                    snackbar.dismiss()
                    onBackPressed()
                }
            }
        })
        simpleExoPlayer.playWhenReady = true
    }

    private fun doubleTapEnable() {
        mainBinding.playerView.player = simpleExoPlayer
        mainBinding.ytOverlay.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationStart() {
                mainBinding.ytOverlay.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                mainBinding.ytOverlay.visibility = View.GONE
            }
        })
        mainBinding.ytOverlay.player(simpleExoPlayer)
    }

    private fun seekBarFeature() {
        controllerBinding.exoProgress.addListener(object : OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                simpleExoPlayer.pause()
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                simpleExoPlayer.seekTo(position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                simpleExoPlayer.play()
            }
        })
    }

    override fun onVideoClick(allXVideo: AllXVideo) {
        mainBinding.loadNextProgress.visibility = View.VISIBLE
        mainBinding.lblRelated.visibility = View.INVISIBLE
        mainBinding.rvVideo.visibility = View.INVISIBLE
        var nextVideoInfoDetail: InfoDetail? = null
        NetworkBasic.getRetrofit(Consts.URL_LES8).create(AllXInfoApi::class.java)
            .getVideos(allXVideo.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    nextVideoInfoDetail = ParseAllXIInfo.parseDetailVideo(html)
                }

                override fun onError(e: Throwable) {
                    mainBinding.loadNextProgress.visibility = View.INVISIBLE
                    mainBinding.lblRelated.visibility = View.VISIBLE
                    mainBinding.rvVideo.visibility = View.VISIBLE
                    Log.e("TAG", "Load more error: ${e.message}")
                    val snackbar = Snackbar.make(
                        mainBinding.root,
                        "Lỗi video không tồn tại, thử video khác.",
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setTextColor(Color.parseColor("#ffffff"))
                    snackbar.setActionTextColor(Color.parseColor("#ffa500"))
                    snackbar.setAction("Đóng") {
                        snackbar.dismiss()
                    }
                    snackbar.show()
                }

                override fun onComplete() {
                    val intent = intent
                    intent.putExtra("infoDetail", nextVideoInfoDetail)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    finish()
                    startActivity(intent)
                }
            })
    }

    override fun onBackPressed() {
        if (isFullScreen) {
            isFullScreen = false
            setFullScreen(isFullScreen)
            return
        } else {
            if (simpleExoPlayer != null) {
                simpleExoPlayer.release()
            }
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: ")
        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
        //check pIP
        if (isInPictureInPictureMode) {
            simpleExoPlayer.playWhenReady = true
        } else {
            simpleExoPlayer.playWhenReady = false
            simpleExoPlayer.playbackState
        }
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.playbackState
    }

    override fun onRestart() {
        super.onRestart()
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.playbackState
    }

    override fun onDestroy() {
        super.onDestroy()
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration?
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isCrossChecked = isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            mainBinding.playerView.hideController()
        } else {
            mainBinding.playerView.showController()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: ")
        if (isCrossChecked) {
            simpleExoPlayer.release()
            finish()
        }
    }
}