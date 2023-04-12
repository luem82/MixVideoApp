package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentHDTubeVideosBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class HDTubeVideosFragment : Fragment(), HDTubeVideoAdapter.HDTubeVideoListener {

    private lateinit var binding: FragmentHDTubeVideosBinding
    private var mSub = HDConsts.PATH_VIDEOS
    private var mFilter = HDConsts.SORT_POPULAR
    private var mPage = 1
    private lateinit var mAdapter: HDTubeVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_h_d_tube_videos, container, false)
        binding = FragmentHDTubeVideosBinding.bind(view)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = HDTubeVideosFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initSwipeRefreshLayout()
        loadData()

        //filter video
        binding.ivFilter.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_filter_hdtube, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.filter_hdtube_latest -> {
                        filterVideos(HDConsts.SORT_LATEST_UPDATES, HDConsts.PATH_VIDEOS)
                        true
                    }
                    R.id.filter_hdtube_rated -> {
                        filterVideos(HDConsts.SORT_TOP_RATED, HDConsts.PATH_VIDEOS)
                        true
                    }
                    R.id.filter_hdtube_year -> {
                        filterVideos(HDConsts.SORT_YEAR_22, HDConsts.PATH_BEST)
                        true
                    }
                    R.id.filter_hdtube_popular -> {
                        filterVideos("", HDConsts.PATH_VIDEOS)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun filterVideos(sort: String, sub: String) {
        mFilter = sort
        mSub = sub
        mPage = 1
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = true
        binding.ivFilter.visibility = View.GONE
        loadData()
    }

    private fun initAdapter() {
        mAdapter = HDTubeVideoAdapter(R.layout.item_video_hdtube, null, this)
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
        val isRefresh = mPage == 1
        //    https://www.hdtube.porn/videos/latest-updates/2/ -> latest updates
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlVideos(mSub, mFilter, mPage)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHDTube.parseVideos(html)
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
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "Load more error: ${e.message}")
                    if (e.message!!.contains("HTTP 404")) {
                        mAdapter.loadMoreEnd(true)
                    }
                }

                override fun onComplete() {
                    binding.swipeRefresh.isRefreshing = false
                    binding.ivFilter.visibility = View.VISIBLE
                }
            })
    }

    override fun onVideoClick(hdTubeVideo: HDTubeVideo) {
        val intent = Intent(context, HDTubeVideoDetailActivity::class.java)
        intent.putExtra("HDTubeVideo", hdTubeVideo)
        startActivity(intent)
    }
}