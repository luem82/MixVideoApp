package com.example.mixvideoapp.xcuto

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityWebExoBinding
import com.example.mixvideoapp.databinding.CustomControllerViewBinding
import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.video.VideoSize
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup

class WebExoActivity : AppCompatActivity(), VertiAdapter.VertiVideoListener {

    private val TAG = "WebExoActivity"
    private lateinit var binding: ActivityWebExoBinding
    private lateinit var mixVideo: MixVideo
    private lateinit var controllerBinding: CustomControllerViewBinding
    private lateinit var player: SimpleExoPlayer
    private var isFullscreen = false
    private lateinit var sourceUri: String
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebExoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mixVideo = intent.getSerializableExtra("MixVideo") as MixVideo
        initWebView()
        initRelatedVideo()
    }

    private fun initRelatedVideo() {
        var data = ArrayList<MixVideo>()
        var description = ""
        val url = mixVideo.href.replace("embed/", "")
        NetworkBasic.getRetrofit(Consts.URL_CUTO).create(CutoApi::class.java)
            .getVideoDetail(url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    data = ParseHTML.parseVideos(mixVideo.type, html) as ArrayList<MixVideo>
                    description = Jsoup.parse(html).select("div[class=text descr]").text()
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(this@WebExoActivity, e.message, Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                    displayMoreVideos(data, description)
                }
            })
    }

    private fun displayMoreVideos(data: ArrayList<MixVideo>, description: String) {
        binding.tvTitle.text = mixVideo.title
        binding.tvDescription.text = description
        val videoAdapter = VertiAdapter(R.layout.item_video_favorite, data, this)
        videoAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = videoAdapter
    }

    override fun onPlayVideo(mixVideo: MixVideo) {
        if (binding.frameLoading.visibility == View.GONE) {
            val intent = intent
            intent.putExtra("MixVideo", mixVideo)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            finish()
            startActivity(intent)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @Suppress("DEPRECATION")
    private fun initWebView() {

        Glide.with(this)
            .load(mixVideo.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivPoster)

        player = SimpleExoPlayer.Builder(this).build()

        val url = mixVideo.href.replace("/embed", "")
        val settings = binding.webView.settings
        settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36"
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.pluginState = WebSettings.PluginState.ON
        settings.saveFormData = true
        settings.setSupportZoom(true)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
        }

        binding.webView.webChromeClient = object : WebChromeClient() {}

        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                // mimic onClick() event on the center of the WebView
                val delta: Long = 100
                val downTime = SystemClock.uptimeMillis()
                val x = (view.left + view.width / 2).toFloat()
                val y = (view.top + view.height / 2).toFloat()

                val tapDownEvent = MotionEvent.obtain(
                    downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0
                )
                tapDownEvent.source = InputDevice.SOURCE_CLASS_POINTER
                val tapUpEvent = MotionEvent.obtain(
                    downTime, downTime + delta + 2, MotionEvent.ACTION_UP, x, y, 0
                )
                tapUpEvent.source = InputDevice.SOURCE_CLASS_POINTER

                view.dispatchTouchEvent(tapDownEvent)
                view.dispatchTouchEvent(tapUpEvent)
            }

            override fun shouldInterceptRequest(
                view: WebView?, request: WebResourceRequest?
            ): WebResourceResponse? {
                //https://sexcuto.me/get_file/1/d16104751e9fe4fd5be039ed9e7743ea/19000/19669/19669_360p.mp4/?rnd=1673930032799
                if (request!!.url.toString().contains("https://sexcuto.me/get_file")) {
                    Log.e(TAG, "shouldOverrideUrlLoading: ${request?.url}")
                    runnable = Runnable {
                        sourceUri = request?.url.toString()
                        view?.onPause()
                        initPlayer()
                    }
                    Handler(Looper.getMainLooper()).postDelayed(runnable, 500)
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        binding.webView.loadUrl(url)
//        binding.webView.loadUrl(url)
    }

    private fun initPlayer() {
        Handler(Looper.getMainLooper()).removeCallbacks(runnable)

        controllerBinding = CustomControllerViewBinding.bind(findViewById(R.id.custom_controller))
        controllerBinding.titleVideo.text = mixVideo.title
        controllerBinding.titleVideo.visibility = View.INVISIBLE

        binding.playerView.setPlayer(player)
//        val mediaItem = MediaItem.fromUri("https://www.lesbian8.com/get_file/1/80a1aa34f5cf92884e321f5fab4836857a13ba5acc/201000/201058/201058.mp4/?br=1330&embed=true&rnd=1674036360749")
//        val mediaItem = MediaItem.fromUri("https://sexcuto.me/get_file/1/da031713c9b4121a859ba96c13274ea0/19000/19669/19669.mp4/?embed=true&rnd=1674037062881")
        val mediaItem = MediaItem.fromUri(sourceUri)
        player.setMediaItem(mediaItem)
        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.height > videoSize.width) {
                    //portrait
                } else {
                    //landscape
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val snackbar = Snackbar.make(
                    binding.root, "Lỗi trình phát video.", Snackbar.LENGTH_INDEFINITE
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
                    binding.frameLoading.visibility = View.GONE
                }
            }
        })
        player.prepare()
        player.play()

        controllerBinding.fullScreen.setOnClickListener { v ->
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
            controllerBinding.titleVideo.visibility = View.VISIBLE
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_fullscreen_exit)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            setSystemUIVisibility(fullscreen)
            val params = binding.playerView.layoutParams as ConstraintLayout.LayoutParams
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            binding.playerView.layoutParams = params
        } else {
            controllerBinding.titleVideo.visibility = View.INVISIBLE
            controllerBinding.fullScreen.setImageResource(R.drawable.ic_fullscreen)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isFullscreen = true
            setFullscreen(isFullscreen)
        } else {
            isFullscreen = false
            setFullscreen(isFullscreen)
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

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: ")
        //clear all web data
        binding.webView.apply {
            clearMatches()
            clearHistory()
            clearFormData()
            clearSslPreferences()
            clearCache(true)

//            CookieManager.getInstance().removeAllCookies(null)
            if (Build.VERSION.SDK_INT >= 21) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } else {
                CookieManager.getInstance().removeAllCookie()
            }
            WebStorage.getInstance().deleteAllData()
        }
        binding.webView.destroy()

        if (player != null) {
            player.release()
        }
        super.onDestroy()
    }
}