package com.example.mixvideoapp.hdtube

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentHDTubeTagsBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class HDTubeTagsFragment : Fragment() {

    private lateinit var binding: FragmentHDTubeTagsBinding
    private lateinit var mAdapter: HDTubeTagAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_h_d_tube_tags, container, false)
        binding = FragmentHDTubeTagsBinding.bind(view)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = HDTubeTagsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = HDTubeTagAdapter(R.layout.item_tag_hdtube_vertical, null)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.adapter = mAdapter
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlAllTags().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    val data = ParseHDTube.parseTags(html)
                    Collections.shuffle(data)
                    mAdapter.setNewData(data)
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