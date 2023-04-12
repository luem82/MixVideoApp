package com.example.mixvideoapp.xcuto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentSearchCutoBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers


class SearchCutoFragment : Fragment() {

    private lateinit var binding: FragmentSearchCutoBinding
    private var mPage = 1
    private var mQuey = ""
    private lateinit var mAdapter: CutoVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_cuto, container, false)
        binding = FragmentSearchCutoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchCutoFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initSwipeRefreshLayout()

//        var searchText: EditText? = binding.searchView.findViewById(androidx.appcompat.R.id.search_src_text)
//        searchText?.getTextCursorDrawable()?.setTint(ContextCompat.getColor(requireContext(), R.color.purple_700))

        binding.searchView.onActionViewExpanded()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                mQuey = text!!.trim()
                searchVideos()
                // hide keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun initAdapter() {
        mAdapter = CutoVideoAdapter(R.layout.item_video_cuto, null, context)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            val url =
                "https://sexcuto.me/search/${mQuey}?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=${mQuey}&category_ids=&sort_by=post_date&from_videos=${mPage}&from_albums=${mPage}"
            loadData(url)
        }, binding.rvVideo)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            mPage = 1
            val url =
                "https://sexcuto.me/search/${mQuey}?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=${mQuey}&category_ids=&sort_by=post_date&from_videos=${mPage}&from_albums=${mPage}"
            loadData(url)
        }
    }

    private fun searchVideos() {
        //https://sexcuto.me/search/n%C6%B0%E1%BB%9Bc?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=n%C6%B0%E1%BB%9Bc&category_ids=&sort_by=post_date&from_videos=1&from_albums=1
        if (!mAdapter.data.isEmpty()) {
            mAdapter.setNewData(null)
            binding.rvVideo.removeAllViews()
            mAdapter.notifyDataSetChanged()
        }
        mPage = 1
        binding.swipeRefresh.isRefreshing = true
        val url =
            "https://sexcuto.me/search/${mQuey}?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=${mQuey}&category_ids=&sort_by=post_date&from_videos=${mPage}&from_albums=${mPage}"
        loadData(url)
        Log.e("searchVideos", url)
    }

    private fun loadData(url: String) {
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_CUTO).create(CutoApi::class.java)
            .searchVVideos(url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHTML.parseVideos(Consts.TYPE_CUTO, html)
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
            })
    }
}