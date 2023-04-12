package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityHdtubeModelDetailBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class HDTubeModelDetailActivity : AppCompatActivity(), HDTubeVideoAdapter.HDTubeVideoListener {

    private lateinit var binding: ActivityHdtubeModelDetailBinding
    private lateinit var hdTubeModel: HDTubeModel
    private lateinit var hdTubeModelInfo: HDTubeModelInfo
    private var mPage = 1
    private lateinit var mAdapter: HDTubeVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHdtubeModelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hdTubeModel = intent.getSerializableExtra("HDTubeModel") as HDTubeModel
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        Glide.with(this)
            .load(hdTubeModel.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(120, 160)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.modelThumb)
        binding.modelName.text = hdTubeModel.name
        loadInfo()
    }

    private fun loadInfo() {
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlInfoModel(hdTubeModel.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    hdTubeModelInfo = ParseHDTube.parseInfoModel(html)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(this@HDTubeModelDetailActivity, "Error", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("TAG", "Load more error: ${e.message}")
                    if (e.message!!.contains("HTTP 404")) {
                    }
                }

                override fun onComplete() {
                    loadVideo()
                }
            })
    }

    private fun loadVideo() {
        binding.modelBirthday.text = "Birthday: ${hdTubeModelInfo.birthday}"
        binding.modelAge.text = "Age: ${hdTubeModelInfo.age}"
        binding.modelPlace.text = "Place of birth: ${hdTubeModelInfo.place}"
        binding.modelRate.rating = hdTubeModelInfo.rate

        initAdapter()
        loadData()
    }


    private fun initAdapter() {
        mAdapter = HDTubeVideoAdapter(R.layout.item_video_hdtube, null, this)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            loadData()
        }, binding.rvVideo)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun loadData() {
        val url = "${hdTubeModel.href}${mPage}/"
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlVideosByUrl(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    val data: List<HDTubeVideo> = ParseHDTube.parseVideos(html)
                    Log.e("TAG", "load " + data.size + " new videos")
                    if (isRefresh) {
                        mAdapter.setNewData(data)
                        mAdapter.setEnableLoadMore(true)
                    } else {
                        mAdapter.addData(data)
                    }
                    if (data.size < 1) {
                        mAdapter.loadMoreEnd(true)
                    } else if (!isRefresh) {
                        mAdapter.loadMoreComplete()
                    }
                }

                override fun onError(e: Throwable) {
//                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "Load more error: ${e.message}")
                    if (e.message!!.contains("HTTP 404")) {
                        mAdapter.loadMoreEnd(true)
                    }
                }

                override fun onComplete() {
                    binding.pbLoad.visibility = View.GONE
                }
            })
    }

    override fun onVideoClick(hdTubeVideo: HDTubeVideo) {
        val intent = Intent(this, HDTubeVideoDetailActivity::class.java)
        intent.putExtra("HDTubeVideo", hdTubeVideo)
        startActivity(intent)
    }

}