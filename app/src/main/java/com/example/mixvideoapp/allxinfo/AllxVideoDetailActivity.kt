package com.example.mixvideoapp.allxinfo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityAllxVideoDetailBinding
import com.example.mixvideoapp.util.Consts
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class AllxVideoDetailActivity : AppCompatActivity(), AllXVideoAdapter.IVideoListener {

    private lateinit var binding: ActivityAllxVideoDetailBinding
    private lateinit var allXVideo: AllXVideo
    private lateinit var infoDetail: InfoDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllxVideoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        allXVideo = intent.getSerializableExtra("AllXVideo") as AllXVideo
        initVideo()
        getInfo()
    }

    private fun getInfo() {
        NetworkBasic.getRetrofit(Consts.URL_LES8).create(AllXInfoApi::class.java)
            .getVideos(allXVideo.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    infoDetail = ParseAllXIInfo.parseDetailVideo(html)
                }

                override fun onError(e: Throwable) {
                    Log.e("TAG", "Load more error: ${e.message}")
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Lỗi video.",
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
                    displayInfoDetail()
                }
            })
    }

    private fun displayInfoDetail() {
        binding.progressBar.visibility = View.GONE
        binding.ivPlay.visibility = View.VISIBLE
        binding.tvVideoDesc.text = infoDetail.videoDescription
        val videoAdapter =
            AllXVideoAdapter(R.layout.item_video_allxinfo, infoDetail.listRelated, this)
        videoAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = videoAdapter

        binding.ivPlay.setOnClickListener {
            val intent = Intent(this, AllInfoPlayerActivity::class.java)
            intent.putExtra("infoDetail", infoDetail)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun initVideo() {
        setSupportActionBar(binding.toolBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { v -> onBackPressed() }
        binding.appBarLayout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                val rank = Math.abs(verticalOffset) - appBarLayout!!.totalScrollRange
                if (rank == 0) {
                    //  Collapsed
                    binding.collapsingToolbarLayout.title = allXVideo.title
                } else {
                    //Expanded
                    binding.collapsingToolbarLayout.title = null
                }
            }
        })


        Glide.with(this)
            .load(allXVideo.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(240, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.videoThumb)

        binding.tvVideoTitle.text = allXVideo.title
        binding.tvVideoDate.text = "Đã đăng: ${allXVideo.date}"
        binding.tvVideoViews.text = allXVideo.views
        binding.tvVideoRates.text = allXVideo.rates
        binding.tvVideoDuration.text = allXVideo.duration
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onVideoClick(allXVideo: AllXVideo) {
        val intent = intent
        intent.putExtra("AllXVideo", allXVideo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        finish()
        startActivity(intent)
    }

}