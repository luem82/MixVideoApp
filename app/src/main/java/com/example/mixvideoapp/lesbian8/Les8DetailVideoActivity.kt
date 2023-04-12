package com.example.mixvideoapp.lesbian8

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.ApiMix
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.adapter.PhotoAdapter
import com.example.mixvideoapp.databinding.ActivityLes8DetailVideoBinding
import com.example.mixvideoapp.model.MixPhoto
import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import com.example.mixvideoapp.xcuto.WebViewActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class Les8DetailVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLes8DetailVideoBinding
    private lateinit var mixVideo: MixVideo
    private lateinit var mAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLes8DetailVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mixVideo = intent.getSerializableExtra("MixVideo") as MixVideo
        initVideoPreview()
        initScreenshots()
    }

    private fun initVideoPreview() {

        Glide.with(this)
            .load(mixVideo.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.lesVideoThumb)
        binding.tvTitle.text = mixVideo.title
        binding.tvDate.text = mixVideo.time
        binding.tvDuration.text = mixVideo.duration

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.tvPreview.setOnClickListener {
            playTrailer()
        }

        binding.btnFull.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("embed_url", mixVideo.href)
            intent.putExtra("video_title", mixVideo.title)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun playTrailer() {
        if (mixVideo.preview == null) {
            return
        }

        binding.videoPreview.visibility = View.VISIBLE
        binding.pbPreview.visibility = View.VISIBLE
        binding.videoPreview.setVideoURI(Uri.parse(mixVideo.preview))
        binding.videoPreview.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
            if (mp.isPlaying) {
                binding.pbPreview.visibility = View.GONE
            } else {
                binding.pbPreview.visibility = View.VISIBLE
            }
        }
        binding.videoPreview.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            binding.pbPreview.visibility = View.GONE
            binding.videoPreview.visibility = View.GONE
            Toast.makeText(this, "Video lỗi", Toast.LENGTH_SHORT).show()
            true
        }
        binding.videoPreview.setOnCompletionListener { mp: MediaPlayer? ->
            binding.videoPreview!!.visibility = View.GONE
        }
        binding.videoPreview.start()
    }

    private fun initScreenshots() {
        mAdapter = PhotoAdapter(R.layout.item_photo_grid, null, null)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvPhoto.setHasFixedSize(true)
        binding.rvPhoto.adapter = mAdapter

        NetworkBasic.getRetrofit(Consts.URL_LES8).create(Les8Api::class.java)
            .getScreenshots(mixVideo.link!!)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHTML.parsePhotos("getScreenshots", html)
                    mAdapter.setNewData(data)
                    if (data.isNullOrEmpty()) binding.tvEmpty.visibility = View.VISIBLE
                }

                override fun onError(e: Throwable) {
                    Log.e("TAG", "Load more error: ${e.message}")
                    binding.pbScreem.visibility = View.GONE

                    binding.tvEmpty.visibility = View.VISIBLE
                }

                override fun onComplete() {
                    binding.pbScreem.visibility = View.GONE
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}