package com.example.mixvideoapp.xcuto

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.databinding.ActivityWebViewBinding
import com.example.mixvideoapp.util.Consts
import com.google.android.material.snackbar.Snackbar


@SuppressLint("ObsoleteSdkInt", "NewApi")
@Suppress("DEPRECATION")
class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var runnable: Runnable
    private lateinit var embedUrl: String
    private lateinit var videoTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFullscreen()
        initWebView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initWebView() {

//        embedUrl = "https://www.lesbian8.com/embed/201058"
//        embedUrl = "https://sexcuto.me/embed/19669"
        embedUrl = intent.getStringExtra("embed_url").toString()
        videoTitle = intent.getStringExtra("video_title").toString()
        binding.webView.setBackgroundColor(Color.parseColor("#1C1C1C"))
        binding.tvTitle.text = videoTitle

        binding.webView.apply {
            //                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
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

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    Log.e("WebResourceResponse", "shouldInterceptRequest: ${request!!.url}")

                    //https://www.lesbian8.com/get_file/1/80a1aa34f5cf92884e321f5fab4836857a13ba5acc/201000/201058/201058.mp4/?br=1330&embed=true&rnd=1674036360749
                    if (request!!.url.toString().contains("https://www.lesbian8.com/get_file/")) {
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                    // Do something with the event here
                    return true
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    Log.e("WebResourceResponse", "shouldOverrideUrlLoading: ${url}")
                    return if (url!!.contains("www.lesbian8.com")) {
                        // This is my web site, so do not override; let my WebView load the page
                        Log.e("UrlLoading", url)
                        //https://www.lesbian8.com/videos/61284/chloe-foster-and-adrienne-anderson-licking-pussies/
                        val link = url.substringBeforeLast("/").substringAfter("videos/")
                        val id = link.substringBefore("/")
                        val title_video = link.substringAfter("/").replace("-", " ")
                        Log.e("UrlLoading", title_video)
                        val embeb_url = "${Consts.URL_LES8}/embed/${id}/"
                        Log.e("UrlLoading", embeb_url)
                        view?.loadUrl(embeb_url)
                        binding.tvTitle.text = title_video
                        false
                    } else true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.pbWebview.visibility = View.GONE
                    // auto play
//                    autoPlayVideo(view)
                }
            }

            if (embedUrl != null) {
                loadUrl(embedUrl)
//                loadUrl("https://fr.eporner.com/embed/zSPrx5t9j8Y/")
            } else {
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

//            loadUrl("https://thuyetminhviet.com/movies/ke-dam-len-glass-onion-2022/")
//            loadUrl("https://sexcuto.me/embed/19669")
//            loadUrl("https://fr.eporner.com/embed/j37EOZzJzNz/")
//            loadUrl("https://www.lesbian8.com/embed/152862")
//            loadUrl("https://www.lesbian8.com/embed/201058")
        }


        binding.webView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setTopBarVisibility()
                false
            } else false
        }

        binding.ivBack.setOnClickListener { onBackPressed() }
    }

    private fun setFullscreen() {
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

    private fun setTopBarVisibility() {
        runnable = Runnable {
            if (binding.topNav.visibility == View.INVISIBLE) {
                binding.topNav.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed(runnable, 4000)
            } else {
                binding.topNav.visibility = View.INVISIBLE
            }
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun autoPlayVideo(view: WebView?) {
        // auto play - mimic onClick() event on the center of the WebView
        val delta: Long = 100
        val downTime: Long = SystemClock.uptimeMillis()
        val x = (view!!.left + view.width / 2).toFloat()
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

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    public override fun onPause() {
        binding.webView.onPause()
        binding.webView.pauseTimers()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        binding.webView.resumeTimers()
        binding.webView.onResume()
    }

    public override fun onDestroy() {
        Log.e("webView", "onDestroy: ")
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
        super.onDestroy()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setSystemUIVisibility(hide: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val window = window.insetsController!!
            val windows = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()

//            if (hide) window.hide(windows) else window.show(windows)

            if (hide) {
                window.hide(windows)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                window.show(windows)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            // needed for hide, doesn't do anything in show
            window.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            val view = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

//            window.decorView.systemUiVisibility = if (hide) view else view.inv()

            if (hide) {
                window.decorView.systemUiVisibility = view
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                window.decorView.systemUiVisibility = view.inv()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
}