package com.example.mixvideoapp.pornogids

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentGidsCategoryListBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class GidsCategoryListFragment : Fragment() {

    private lateinit var binding: FragmentGidsCategoryListBinding
    private lateinit var mAdapter: GidsCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_gids_category_list, container, false)
        binding = FragmentGidsCategoryListBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = GidsCategoryListFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        initSwipeRefreshLayout()
        loadData()
    }

    private fun initAdapter() {
        mAdapter = GidsCategoryAdapter(R.layout.item_category_gids, null)
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
        NetworkBasic.getRetrofit("https://pornogids.net/")
            .create(GidsApi::class.java).getCategories()
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseGids.parseCategories(html)
                    mAdapter.addData(data)
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
                    mAdapter.filter.filter(newText)
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
}





