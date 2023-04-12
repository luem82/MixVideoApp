package com.example.mixvideoapp.xcuto

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityCutoCategoryBinding
import com.example.mixvideoapp.model.MixCategory
import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class CutoCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCutoCategoryBinding
    private lateinit var mixCategory: MixCategory
    private var mPage = 1
    private lateinit var mAdapter: CutoVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCutoCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mixCategory = intent.getSerializableExtra("MixCategory") as MixCategory
        initCollapsingToolbar()
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initCollapsingToolbar() {
        Glide.with(this)
            .load(mixCategory.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivHeader)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = mixCategory.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initAdapter() {
        mAdapter = CutoVideoAdapter(R.layout.item_video_cuto, null, this)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            loadData()
        }, binding.rvVideo)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
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
        var url = "${mixCategory.href}/${mPage}"
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_CUTO).create(CutoApi::class.java)
            .getVideosByCategory(url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    val data: List<MixVideo> = ParseHTML.parseVideos(Consts.TYPE_CUTO, html)
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
}