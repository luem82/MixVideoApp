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
import com.example.mixvideoapp.adapter.AlbumAdapter
import com.example.mixvideoapp.databinding.FragmentListAlbumBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.Helpers
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListAlbumFragment : Fragment() {

    private var mType: String? = null
    private var mUrl = ""
    private var mPage = 1
    private lateinit var mAdapter: AlbumAdapter
    private lateinit var binding: FragmentListAlbumBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_album, container, false)
        binding = FragmentListAlbumBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            ListAlbumFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mUrl = Helpers.createUrlGetAlbums(mType!!, mPage)
        Log.e("ListAlbumFragment", "ListAlbumFragment: ${mUrl}")
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = AlbumAdapter(R.layout.item_album_big, null)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            mUrl = Helpers.createUrlGetAlbums(mType!!, mPage)
            loadData()
        }, binding.rvAlbum)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvAlbum.setHasFixedSize(true)
        binding.rvAlbum.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            mPage = 1
            mUrl = Helpers.createUrlGetAlbums(mType!!, mPage)
            loadData()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_AUDIO).create(ApiMix::class.java)
            .getAlbums(mUrl).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHTML.parseAlbums(mType!!, html)
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