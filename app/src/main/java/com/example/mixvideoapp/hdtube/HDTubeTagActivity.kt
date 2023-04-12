package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityHdtubeTagBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class HDTubeTagActivity : AppCompatActivity(), HDTubeVideoAdapter.HDTubeVideoListener {

    private lateinit var binding: ActivityHdtubeTagBinding
    private lateinit var tagName: String
    private var mPage = 1
    private lateinit var mAdapter: HDTubeVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHdtubeTagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tagName = intent.getStringExtra("tag_name").toString()
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = tagName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
        initAdapter()
        initSwipeRefreshLayout()
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

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            mPage = 1
            loadData()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        // https://www.hdtube.porn/search/mother+and+son/2/ -> search
        val query = tagName.replace(" ", "+")
        val url = "https://www.hdtube.porn/search/${query}/${mPage}/"
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
                    binding.swipeRefresh.isRefreshing = false
                }
            })
    }

    override fun onVideoClick(hdTubeVideo: HDTubeVideo) {
        val intent = Intent(this, HDTubeVideoDetailActivity::class.java)
        intent.putExtra("HDTubeVideo", hdTubeVideo)
        startActivity(intent)
    }
}