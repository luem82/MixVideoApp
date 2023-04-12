package com.example.mixvideoapp.allxinfo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityAllXcategoryDetailBinding
import com.example.mixvideoapp.util.Consts
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class AllXCategoryDetailActivity : AppCompatActivity(), AllXVideoAdapter.IVideoListener {

    private lateinit var binding: ActivityAllXcategoryDetailBinding
    private lateinit var allXCategory: AllXCategory
    private var mPage = 1
    private lateinit var mAdapter: AllXVideoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllXcategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        allXCategory = intent.getSerializableExtra("AllXCategory") as AllXCategory
        initCollapsingToolbar()
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }


    private fun initCollapsingToolbar() {
        Glide.with(this)
            .load(allXCategory.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
//            .override(175, 215)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivHeader)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = allXCategory.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initAdapter() {
        mAdapter = AllXVideoAdapter(R.layout.item_video_allxinfo, null, this)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            loadData()
        }, binding.rvVideo)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            mPage = 1
            loadData()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        var url = "${allXCategory.href}${mPage}/"
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_LES8).create(AllXInfoApi::class.java)
            .getVideos(url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    val data: List<AllXVideo> = ParseAllXIInfo.parseVideos(html)
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
                    binding.swipeRefresh.isRefreshing = false
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onVideoClick(allXVideo: AllXVideo) {
        val intent = Intent(this, AllxVideoDetailActivity::class.java)
        intent.putExtra("AllXVideo", allXVideo)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}