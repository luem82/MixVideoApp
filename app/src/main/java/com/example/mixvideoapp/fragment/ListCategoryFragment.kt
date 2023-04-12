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
import com.example.mixvideoapp.adapter.CategoryAdapter
import com.example.mixvideoapp.databinding.FragmentListCategoryBinding
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListCategoryFragment : Fragment() {

    private var mType: String? = null
    private var mUrl: String? = null
    private lateinit var binding: FragmentListCategoryBinding
    private lateinit var mAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(ARG_PARAM1)
            mUrl = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_category, container, false)
        binding = FragmentListCategoryBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String, url: String) =
            ListCategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, type)
                    putString(ARG_PARAM2, url)
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
        mAdapter = CategoryAdapter(R.layout.item_category, null)
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
        NetworkBasic.getRetrofit("https://xvideos98.xxx/")
            .create(ApiMix::class.java)
            .getCategories(mUrl!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHTML.parseCategories(mType!!, html)
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