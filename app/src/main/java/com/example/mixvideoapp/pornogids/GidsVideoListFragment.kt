package com.example.mixvideoapp.pornogids

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentGidsVideoListBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

class GidsVideoListFragment : Fragment(), GidsVideoAdapter.IGidsVideoListener {

    private val TAG = "gids_video"
    private var mType: String? = null
    private var mFilter: String? = null
    private var mCategory: String? = null
    private var mQuery: String? = null
    private lateinit var binding: FragmentGidsVideoListBinding
    private val BASE_URL = "https://pornogids.net/"
    private var mPage = 1
    private lateinit var mAdapter: GidsVideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(ARG_PARAM1)
            mFilter = it.getString(ARG_PARAM2)
            mCategory = it.getString(ARG_PARAM3)
            mQuery = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_gids_video_list, container, false)
        binding = FragmentGidsVideoListBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = GidsVideoAdapter(R.layout.item_video_gids, null, this)
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
        val isRefresh = mPage == 1
        getObservable().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseGids.parseVideos(html)
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
                    Toast.makeText(context, "Lỗi: " + e.message, Toast.LENGTH_SHORT).show()
                    mAdapter.loadMoreEnd(true)
                }

                override fun onComplete() {
                    binding.swipeRefresh.isRefreshing = false
                }
            })
    }


    private fun getObservable(): Observable<String> {
        return when (mType) {
            "first" -> {
                NetworkBasic.getRetrofit(BASE_URL)
                    .create(GidsApi::class.java)
                    .getVideosFirst(mFilter!!, mPage)
            }
            "search" -> {
                NetworkBasic.getRetrofit(BASE_URL)
                    .create(GidsApi::class.java)
                    .searchVideos(mQuery!!, mPage)
            }
            else -> {
                NetworkBasic.getRetrofit(BASE_URL)
                    .create(GidsApi::class.java)
                    .getVideosByCategory(mCategory!!, mPage)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.gids_filter_new -> {
                mFilter = "latest-updates"
                filterVideos()
                true
            }
            R.id.gids_filter_popular -> {
                mFilter = "most-popular"
                filterVideos()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun filterVideos() {
        Log.e(TAG, "filterVideos: ${mFilter}")
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = true
        mPage = 1
        loadData()
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String?, filter: String?, category: String?, query: String?) =
            GidsVideoListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, type)
                    putString(ARG_PARAM2, filter)
                    putString(ARG_PARAM3, category)
                    putString(ARG_PARAM4, query)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem = menu?.findItem(R.id.act_search_gids)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(requireContext())

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView!!.queryHint = "Nhập tiếng anh"
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

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    val key = query.trim().replace(" ", "-")
                    val title = query.trim()
                    if (!key.isNullOrEmpty()) {
                        searchVideo(key, title)
                        // hide keyboard
                        (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
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
    }


    private fun searchVideo(key: String, title: String) {
        val intent = Intent(context, SearchGidsActivity::class.java)
        intent.putExtra("key", key)
        intent.putExtra("title", title)
        startActivity(intent)
        (requireActivity() as AppCompatActivity).overridePendingTransition(
            android.R.anim.fade_in, android.R.anim.fade_out
        )
    }

    override fun onGidsVideoClick(gidsVideo: GidsVideo) {
        val intent = Intent(context, GidsPlayerActivity::class.java)
        intent.putExtra("GidsVideo", gidsVideo)
        startActivity(intent)
        (requireActivity() as AppCompatActivity).overridePendingTransition(
            android.R.anim.fade_in, android.R.anim.fade_out
        )
    }
}