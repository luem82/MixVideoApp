package com.example.mixvideoapp.allxinfo

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.mixvideoapp.R
import com.example.mixvideoapp.allxinfo.AllXInfoActivity.Companion.ALLX_URL
import com.example.mixvideoapp.databinding.ActivitySearchAllXinfoBinding

class SearchAllXInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchAllXinfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAllXinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.act_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)
        searchItem!!.expandActionView()

        if (searchItem != null) {
            searchView = searchItem.actionView as SearchView
            searchView.queryHint = "Nhập từ khóa tìm kiếm"
        }

        if (searchView != null) {

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (!query.isNullOrEmpty()) {
                        replaceFragment(query.trim().replace(" ", "-"))
                        // hide keyboard
                        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(searchView.windowToken, 0)
                        searchView.clearFocus()
                        return true
                    } else {
                        return false
                    }
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return true
                }
            })

        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun replaceFragment(query: String) {
        //https://allx.info/search/con-trai/
        val url = ALLX_URL + "/search/${query}/"
        Log.e("TAG", "replaceFragment: ${url}")
        val fragment = ListVideoAllXInfoFragment.newInstance(url)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_search, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}