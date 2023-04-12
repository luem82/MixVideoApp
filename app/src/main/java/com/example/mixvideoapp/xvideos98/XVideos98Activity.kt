package com.example.mixvideoapp.xvideos98

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityXvideos98Binding
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class XVideos98Activity : AppCompatActivity(), Xvideos98Adapter.IXvideos98Listener {

    private lateinit var binding: ActivityXvideos98Binding
    private var isSearch = false
    private var mSort: String? = null
    private var mQuery = ""
    private var mPage = 0
    private lateinit var mAdapter: Xvideos98Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityXvideos98Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewToolBar()
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = Xvideos98Adapter(R.layout.item_video_x98, null, this)
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
            mPage = 0
            loadData()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        getObservable().subscribe(getObserver())
    }

    private fun getObserver(): Observer<String> {
        val isRefresh = mPage == 0
        return object : DisposableObserver<String>() {
            override fun onNext(html: String) {
                var data = ParseXvideos98.parseVideos(html)
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
                Log.e("TAG", "Load more error: ${e.message}")
                if (e.message!!.contains("HTTP 404")) {
                    mAdapter.loadMoreEnd(true)
                }
            }

            override fun onComplete() {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun getObservable(): Observable<String> {
        return when {
            isSearch -> {
                NetworkBasic.getRetrofit(XConsts.BASE_URL).create(XVideo98Api::class.java)
                    .searchVideos(mQuery, mSort, mPage).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            else -> {
                NetworkBasic.getRetrofit(XConsts.BASE_URL).create(XVideo98Api::class.java)
                    .getVideos(mPage, mSort).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
        }
    }

    private fun initViewToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_xvideos98, menu)
        val searchItem = menu?.findItem(R.id.act_search_x98)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView!!.queryHint = "Nhập tiếng anh | việt"
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView!!.queryHint = ""
                return true
            }
        })

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
        }

        if (searchView != null) {

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val key = query.trim().replace(" ", "+")
                    if (!key.isNullOrEmpty()) {
                        searchVideo(key, XConsts.SORT_DATE)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.x98_filter_date -> {
                filterVideos(XConsts.SORT_DATE)
                true
            }
            R.id.x98_filter_length -> {
                filterVideos(XConsts.SORT_LENGTH)
                true
            }
            R.id.x98_filter_random -> {
                filterVideos(XConsts.SORT_RANDOM)
                true
            }
            R.id.x98_filter_rating -> {
                filterVideos(XConsts.SORT_RATING)
                true
            }
            R.id.x98_filter_views -> {
                filterVideos(XConsts.SORT_VIEWS)
                true
            }
            R.id.act_refresh_x98 -> {
                refreshVideos()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun searchVideo(key: String, sort: String) {
        mQuery = key
        isSearch = true
        mPage = 0
        mSort = sort
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = true
        loadData()
    }

    private fun refreshVideos() {
        isSearch = false
        mPage = 0
        mSort = null
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = true
        loadData()
    }

    private fun filterVideos(sort: String) {
        mPage = 0
        mSort = sort
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = true
        loadData()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onClickXvideos98(xvideos98: Xvideos98) {
        val subList = mAdapter.data.shuffled().subList(0, 18)
        val listMore = arrayListOf<Xvideos98>()
        listMore.addAll(subList)
        val intent = Intent(this, Xvideos98PlayerActivity::class.java)
        intent.putExtra("Xvideos98", xvideos98)
        intent.putExtra("listMore", listMore)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}