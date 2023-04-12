package com.example.mixvideoapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.ApiMix
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.adapter.VideoAdapter
import com.example.mixvideoapp.databinding.FragmentListVideoBinding
import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.Helpers
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class ListVideoFragment : Fragment() {

    private var mType: String? = null
    private var mSort: String? = null
    private var mUrl: String? = null
    private var mQuery: String? = null
    private var mPage = 1
    private lateinit var mAdapter: VideoAdapter
    private lateinit var binding: FragmentListVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(ARG_PARAM1)
            mSort = it.getString(ARG_PARAM2)
            mQuery = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list_video, container, false)
        binding = FragmentListVideoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String, sort: String, query: String) =
            ListVideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, type)
                    putString(ARG_PARAM2, sort)
                    putString(ARG_PARAM3, query)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUrl = Helpers.createUrlGetVideos(mType!!, mSort!!, mPage!!, mQuery!!)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = VideoAdapter(R.layout.item_video_big, null, context)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            mUrl = Helpers.createUrlGetVideos(mType!!, mSort!!, mPage!!, mQuery!!)
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
            mUrl = Helpers.createUrlGetVideos(mType!!, mSort!!, mPage!!, mQuery!!)
            loadData()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        getObservable().subscribe(getObserver())
    }

    private fun getObservable(): Observable<String> {
        return NetworkBasic.getRetrofit("https://xvideos98.xxx/")
            .create(ApiMix::class.java)
            .getVideos(mUrl!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getObserver(): Observer<String> {
        val isRefresh = mPage == 1
        return object : DisposableObserver<String>() {
            override fun onNext(html: String) {
                var data: List<MixVideo>
                if (mType!!.equals(Consts.TYPE_SEARCH)) {
                    data = ParseHTML.parseVideos(mSort!!, html)
                } else {
                    data = ParseHTML.parseVideos(mType!!, html)
                }

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
}