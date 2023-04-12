package com.example.mixvideoapp.xcuto

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentCategoryCutoBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers


class CategoryCutoFragment : Fragment() {

    private var mPage = 1
    private lateinit var mAdapter: CutoCategoryAdapter
    private lateinit var binding: FragmentCategoryCutoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_cuto, container, false)
        binding = FragmentCategoryCutoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = CategoryCutoFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = CutoCategoryAdapter(R.layout.item_category_cuto, null)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            loadData()
        }, binding.rvCategory)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun initSwipeRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
        binding.swipeRefresh.isRefreshing = true
    }

    private fun loadData() {
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_CUTO).create(CutoApi::class.java)
            .getAllCategories(mPage).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    val data = ParseHTML.parseCategories(Consts.TYPE_CUTO, html)
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