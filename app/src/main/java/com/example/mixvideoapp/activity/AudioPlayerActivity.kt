package com.example.mixvideoapp.activity

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.animaltube.network.ApiMix
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityAudioPlayerBinding
import com.example.mixvideoapp.databinding.CustomAudioControllerViewBinding
import com.example.mixvideoapp.model.MixAudio
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
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

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var playerBinding: ActivityAudioPlayerBinding
    private lateinit var controllerBinding: CustomAudioControllerViewBinding
    private lateinit var player: SimpleExoPlayer
    private lateinit var sourceUrl: String
    private lateinit var mixAudio: MixAudio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        playerBinding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(playerBinding.root)

        mixAudio = intent.getSerializableExtra("MixAudio") as MixAudio
        initUI()
        setListeners()
    }

    private fun setListeners() {
        controllerBinding.backAudio.setOnClickListener { onBackPressed() }

        controllerBinding.downloadAudio.setOnClickListener {
            Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        downloadAudio()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", getPackageName(), null)
                        intent.data = uri
                        ActivityCompat.startActivityForResult(
                            this@AudioPlayerActivity, intent, 100, null
                        )
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?, token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun downloadAudio() {
        val request = DownloadManager.Request(Uri.parse(sourceUrl))
        request.setTitle("${mixAudio.title}")
        request.setDescription("Truyện Audio")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS, "${mixAudio.title}.mp3"
        )
        request.allowScanningByMediaScanner()
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(false)

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Snackbar.make(
            playerBinding.root, "Đang tải xuống truyện audio", Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun initUI() {
        Glide.with(this)
            .asBitmap()
            .load(mixAudio.thumb)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .circleCrop()
            .into(playerBinding.audioThumb)
        playerBinding.audioTitle.text = mixAudio.title

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
        controllerBinding = CustomAudioControllerViewBinding.bind(
            findViewById(R.id.custom_audio_controller)
        )
        playAudio()
    }

    private fun playAudio() {
        NetworkBasic.getRetrofit(Consts.URL_AUDIO)
            .create(ApiMix::class.java)
            .getAudioSource(mixAudio.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    sourceUrl = ParseHTML.parseAudioSource(html)
                    player = SimpleExoPlayer.Builder(this@AudioPlayerActivity).build()
                    playerBinding.playerView.setPlayer(player)
                    val mediaItem = MediaItem.fromUri(sourceUrl)
                    player.setMediaItem(mediaItem)
                    player.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            val snackbar = Snackbar.make(
                                playerBinding.root,
                                "Lỗi trình phát audio.",
                                Snackbar.LENGTH_INDEFINITE
                            )
                            snackbar.setTextColor(Color.parseColor("#ffffff"))
                            snackbar.setActionTextColor(Color.parseColor("#ffa500"))
                            snackbar.setAction("Quay về") {
                                snackbar.dismiss()
                                onBackPressed()
                            }
                            snackbar.show()
                            controllerBinding.downloadAudio.visibility = View.GONE
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_BUFFERING) {
                                controllerBinding.pbLoading.visibility = View.VISIBLE
                            } else if (playbackState == Player.STATE_READY) {
                                controllerBinding.pbLoading.visibility = View.GONE
                            }
                        }
                    })
                    player.prepare()
                }

                override fun onError(e: Throwable) {
                    val snackbar = Snackbar.make(
                        playerBinding.root, "Lỗi trình phát audio.", Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setTextColor(Color.parseColor("#ffffff"))
                    snackbar.setActionTextColor(Color.parseColor("#ffa500"))
                    snackbar.setAction("Quay về") {
                        snackbar.dismiss()
                        onBackPressed()
                    }
                    snackbar.show()
                    controllerBinding.downloadAudio.visibility = View.GONE
                }

                override fun onComplete() {
                    playerBinding.bigPbLoad.visibility = View.GONE
                    player.play()
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player.release()
        }
    }
}