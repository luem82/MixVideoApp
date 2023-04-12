package com.example.mixvideoapp.lesbian8

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentLes8ListVideoBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"

class Les8ListVideoFragment : Fragment() {

    private var mSort: String? = null
    private var mPage = 1
    private lateinit var mAdapter: Les8VideoAdapter
    private lateinit var binding: FragmentLes8ListVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mSort = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_les8_list_video, container, false)
        binding = FragmentLes8ListVideoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(sort: String) =
            Les8ListVideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, sort)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = Les8VideoAdapter(R.layout.item_video_les, null, context)
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
        NetworkBasic.getRetrofit(Consts.URL_LES8).create(Les8Api::class.java)
            .getVideos(mSort!!, mPage).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHTML.parseVideos(Consts.TYPE_LES8, html)
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