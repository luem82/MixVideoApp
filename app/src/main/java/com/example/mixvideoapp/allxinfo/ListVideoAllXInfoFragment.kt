package com.example.mixvideoapp.allxinfo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.allxinfo.AllXInfoActivity.Companion.ALLX_URL
import com.example.mixvideoapp.databinding.FragmentListVideoAllXInfoBinding
import com.example.mixvideoapp.util.Consts
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ListVideoAllXInfoFragment : Fragment(), AllXVideoAdapter.IVideoListener {

    private var mUrl: String? = null
    private var mPage = 1
    private lateinit var mAdapter: AllXVideoAdapter
    private lateinit var binding: FragmentListVideoAllXInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mUrl = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_video_all_x_info, container, false)
        binding = FragmentListVideoAllXInfoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String) =
            ListVideoAllXInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, url)
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
        mAdapter = AllXVideoAdapter(R.layout.item_video_allxinfo, null, this)
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
        var url = "${mUrl}${mPage}/"
        if (mUrl!!.contains("search/")) {
            val query = mUrl!!.substringAfter("search/").substringBefore("/")
            url =
                ALLX_URL +"/search/${query}/?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=${
                    query.replace(
                        " ",
                        "+"
                    )
                }&category_ids=&sort_by=post_date&from_videos=0${mPage}&from_albums=0${mPage}"

        }
        //https://allx.info/search/me-con/?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=me+con&category_ids=&sort_by=&from_videos=01&from_albums=01
        Log.e("TAG", "replaceFragment: ${url}")
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(Consts.URL_LES8).create(AllXInfoApi::class.java)
            .getVideos(url).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseAllXIInfo.parseVideos(html)
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

    override fun onVideoClick(allXVideo: AllXVideo) {
        val intent = Intent(context, AllxVideoDetailActivity::class.java)
        intent.putExtra("AllXVideo", allXVideo)
        startActivity(intent)
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}





