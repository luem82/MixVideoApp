package com.example.mixvideoapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.animaltube.network.ApiMix
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.MainActivity.Companion.mixRoomDatabase
import com.example.mixvideoapp.databinding.*
import com.example.mixvideoapp.room.Favorite
import com.example.mixvideoapp.room.History
import com.example.mixvideoapp.util.Helpers
import com.example.mixvideoapp.util.ParseHTML
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var playerBinding: ActivityVideoPlayerBinding
    private lateinit var controllerBinding: CustomControllerViewBinding
    private lateinit var player: SimpleExoPlayer
    private var isFullscreen = false
    private var isVideoPortrait = false
    private lateinit var sourceUrl: String
    private var title: String? = null
    private var thumb: String? = null
    private var href: String? = null
    private var duration: String? = null
    private var preview: String? = null
    private var type: String? = null
    private var favorite: Boolean? = null
    private var history: Boolean? = null
    private var current: Long? = null
    private var percent: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        playerBinding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(playerBinding.root)

        getVideo()
        initUI()
        setListeners()
    }

    private fun getVideo() {
        title = intent.getStringExtra("title")
        thumb = intent.getStringExtra("thumb")
        href = intent.getStringExtra("href")
        duration = intent.getStringExtra("duration")
        preview = intent.getStringExtra("preview")
        type = intent.getStringExtra("type")
        favorite = intent.getBooleanExtra("favorite", false)
        history = intent.getBooleanExtra("history", false)
        current = intent.getLongExtra("current", 0)
        percent = intent.getIntExtra("percent", 0)
    }

    private fun setListeners() {

        controllerBinding.backVideo.setOnClickListener { v ->
            onBackPressed()
        }

        controllerBinding.fullScreen.setOnClickListener { v ->
            if (isFullscreen) {
                isFullscreen = false
                setFullscreen(isFullscreen)
            } else {
                isFullscreen = true
                setFullscreen(isFullscreen)
            }
        }

        controllerBinding.favoriteVideo.setOnClickListener { v ->
            if (player.isPlaying) {
                player.pause()
            }

            val view = layoutInflater.inflate(
                R.layout.custom_dialog_favorite, playerBinding.root, false
            )
            val dialogBinding = CustomDialogFavoriteBinding.bind(view)
            var dialog = AppCompatDialog(this)
            dialog.setContentView(view)
            dialog.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
            dialog.show()

            dialogBinding.favCancel.setOnClickListener {
                dialog.dismiss()
                if (!player.isPlaying) {
                    player.play()
                }
            }

            dialogBinding.favSave.setOnClickListener {
                dialog.dismiss()
                if (!player.isPlaying) {
                    player.play()
                }

                // add to favorites
                val favorite = Favorite(
                    null, title!!, thumb!!, duration!!, href!!, preview!!, type!!, true
                )
                mixRoomDatabase.getFavoriteDao().addFavoriteVideo(favorite)

                Snackbar.make(
                    playerBinding.root,
                    "Đã thêm vào danh sách yêu thích.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        }

        controllerBinding.downloadVideo.setOnClickListener { v ->
            if (player.isPlaying) {
                player.pause()
            }
            val view = layoutInflater.inflate(
                R.layout.custom_dialog_download, playerBinding.root, false
            )
            val dialogBinding = CustomDialogDownloadBinding.bind(view)
            var dialog = AppCompatDialog(this)
            dialog.setContentView(view)
            dialog.window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog
            dialog.show()

            dialogBinding.dowPreview.setOnClickListener {
                dialog.dismiss()
                if (!player.isPlaying) {
                    player.play()
                }
                val url = preview!!
                val fileName = "${title!!.trim()}.mp4"
                val description = "Video preview"
                downloadVideo(url, fileName, description)
            }

            dialogBinding.dowFull.setOnClickListener {
                dialog.dismiss()
                if (!player.isPlaying) {
                    player.play()
                }
                val url = sourceUrl
                val fileName = "${title!!.trim()}.mp4"
                val description = "Video đầy đủ"
                downloadVideo(url, fileName, description)
            }
        }
    }

    private fun downloadVideo(url: String, fileName: String, desciption: String) {
        Log.e("downloadVideo", "downloadVideo: ${url} - ${fileName} - ${desciption}")
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setTitle(fileName)
                    request.setDescription(desciption)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName
                    )
                    request.allowScanningByMediaScanner()
                    request.setAllowedOverMetered(true)
                    request.setAllowedOverRoaming(false)

                    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                    Snackbar.make(
                        playerBinding.root, "Đang tải xuống ${desciption}.", Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    ActivityCompat.startActivityForResult(
                        this@VideoPlayerActivity, intent, 100, null
                    )
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?, token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    private fun initUI() {
        //imersie mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsCompat = WindowInsetsControllerCompat(window, playerBinding.root)
            windowInsetsCompat.addOnControllableInsetsChangedListener { controller: WindowInsetsControllerCompat, typeMask: Int ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        controllerBinding = CustomControllerViewBinding.bind(findViewById(R.id.custom_controller))
        if (favorite!!) controllerBinding.favoriteVideo.visibility = View.GONE
        playVideo()
    }

    private fun playVideo() {
        NetworkBasic.getRetrofit("https://xvideos98.xxx/")
            .create(ApiMix::class.java)
            .getVideoSource(href!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    sourceUrl = ParseHTML.parseVideoSource(type!!, html)
                    Log.e("sourceUrl", "onNext: ${sourceUrl}")
                    controllerBinding.titleVideo.setText(title!!)
                    player = SimpleExoPlayer.Builder(this@VideoPlayerActivity).build()
                    playerBinding.playerView.setPlayer(player)
                    val mediaItem = MediaItem.fromUri(sourceUrl)
//                    val mediaItem = MediaItem.fromUri("https://ia801401.us.archive.org/8/items/seri-truyen-cuoc-doi-toi_202109/chuyen-nha-ba-mai.mp3")

                    player.setMediaItem(mediaItem)
                    player.addListener(object : Player.Listener {
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            isVideoPortrait = if (videoSize.height > videoSize.width) {
                                //portrait
                                true
                            } else {
                                //landscape
                                false
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            val snackbar = Snackbar.make(
                                playerBinding.root,
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

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_BUFFERING) {
                                controllerBinding.skLoad.visibility = View.VISIBLE
                            } else if (playbackState == Player.STATE_READY) {
                                controllerBinding.skLoad.visibility = View.GONE
                            }
                        }
                    })
                    player.prepare()
                    if (history!!) player.seekTo(current!!)
                }

                override fun onError(e: Throwable) {
                    val snackbar = Snackbar.make(
                        playerBinding.root, "Lỗi trình phát video.", Snackbar.LENGTH_INDEFINITE
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
                    playerBinding.bigPbLoad.visibility = View.GONE
                    player.play()
                }
            })
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setFullscreen(isFullscreen: Boolean) {
        if (isFullscreen) {
            playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_fullscreen_exit)
            requestedOrientation = if (isVideoPortrait) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        } else {
            playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_fullscreen)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onBackPressed() {
        if (isFullscreen) {
            isFullscreen = false
            setFullscreen(isFullscreen)
            return
        } else {
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            val current = player!!.currentPosition
            val total = player!!.duration
            val percent = Helpers.getPlayerPercent(current, total)
            Log.e("save_video", "onStop: ${current} - ${total}")
            Log.e("save_video", "onStop: ${percent}")
            val history = History(
                null, title!!, thumb!!, duration!!, href!!, preview!!,
                type!!, true, current, percent
            )
            mixRoomDatabase.getHistoryDao().addHistoryVideo(history)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player.release()
        }
    }
}