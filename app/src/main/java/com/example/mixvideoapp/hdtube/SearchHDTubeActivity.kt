package com.example.mixvideoapp.hdtube

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivitySearchHdtubeBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class SearchHDTubeActivity : AppCompatActivity(), HDTubeVideoAdapter.HDTubeVideoListener {

    private lateinit var binding: ActivitySearchHdtubeBinding
    private var mPage = 1
    private lateinit var mQuery: String
    private lateinit var mAdapter: HDTubeVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchHdtubeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = "Search Videos"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
        initAdapter()
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
        val query = mQuery.trim().replace(" ", "+")
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

    private fun searchVideos() {
        if (!mAdapter.data.isNullOrEmpty()) {
            mAdapter.data.clear()
            mAdapter.notifyDataSetChanged()
            mPage = 1
        }
        initSwipeRefreshLayout()
        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.act_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)
        searchItem?.expandActionView()

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            searchView!!.queryHint = "Enter key search"
        }

        if (searchView != null) {

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (!query.isNullOrEmpty()) {
                        mQuery = query
                        searchVideos()
                        // hide keyboard
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(searchView.windowToken, 0)
                        searchView.clearFocus()
                        return true
                    } else {
                        return false
                    }
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })

        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onVideoClick(hdTubeVideo: HDTubeVideo) {
        val intent = Intent(this, HDTubeVideoDetailActivity::class.java)
        intent.putExtra("HDTubeVideo", hdTubeVideo)
        startActivity(intent)
    }

}