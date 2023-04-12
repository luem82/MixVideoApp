package com.example.mixvideoapp.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.ApiMix
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.adapter.VideoAdapter
import com.example.mixvideoapp.databinding.ActivityCategoryBinding
import com.example.mixvideoapp.model.MixCategory
import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var mixCategory: MixCategory
    private var mPage = 1
    private lateinit var mAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
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
            .override(320, 240)
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
        mAdapter = VideoAdapter(R.layout.item_video_big, null, this)
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
        var mHref = ""
        var mType = ""

        if (mixCategory.href.contains("shemaletubevideos")) {
            mHref = "${mixCategory.href.replace(".html", "")}-${mPage}.html"
            mType = Consts.TYPE_TRAN
        } else if (mixCategory.href.contains("xvideos98")) {
            mHref = "${mixCategory.href}${mPage}/"
            mType = Consts.TYPE_VIET
        } else if (mixCategory.href.contains("zoozoosexporn")) {
            mHref = "${mixCategory.href}/${mPage}/"
            mType = Consts.TYPE_ANI
        } else {
            mHref = "${mixCategory.href}${mPage}/"
            mType = Consts.TYPE_EN
        }
        Log.e("loadData", "loadData: ${mHref}")
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit("https://xvideos98.xxx/")
            .create(ApiMix::class.java)
            .getVideos(mHref)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    val data: List<MixVideo> = ParseHTML.parseVideos(mType, html)
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