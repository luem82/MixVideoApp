package com.example.mixvideoapp.activity

import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.MainActivity.Companion.mixRoomDatabase
import com.example.mixvideoapp.adapter.FavoriteAdapter
import com.example.mixvideoapp.databinding.ActivityFavoriteBinding
import com.example.mixvideoapp.interfaces.IVideoFavoriteListener
import com.example.mixvideoapp.room.Favorite
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class FavoriteActivity : AppCompatActivity(), IVideoFavoriteListener {

    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var mAdapter: FavoriteAdapter
    private var clearFavorite: MenuItem? = null
    private var searchFavorite: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
        initAdapter()
    }

    private fun initAdapter() {
        val data = mixRoomDatabase.getFavoriteDao().getFavoriteVideos()
        mAdapter = FavoriteAdapter(R.layout.item_video_favorite, data, this, this)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = mAdapter
        if (mAdapter.data.isNullOrEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.tvEmpty.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_favorite, menu)
        clearFavorite = menu!!.findItem(R.id.act_clear_favorite)
        searchFavorite = menu?.findItem(R.id.act_search_favorite)
        if (mAdapter.data.isNullOrEmpty()) {
            clearFavorite!!.isVisible = false
            searchFavorite!!.isVisible = false
        } else {
            clearFavorite!!.isVisible = true
            searchFavorite!!.isVisible = true
        }
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)

        searchFavorite?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView!!.queryHint = resources.getString(R.string.str_search_favorite)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView!!.queryHint = ""
                return true
            }
        })

        if (searchFavorite != null) {
            searchView = searchFavorite!!.actionView as SearchView
        }

        if (searchView != null) {

            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    mAdapter.filter.filter(newText)
                    return true
                }
            })

        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.act_clear_favorite) {
            val dialog = MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_clear_favorite)
                .setTitle("Xóa Tất Cả")
                .setMessage("Bạn có muốn xóa tất video yêu thích hay không ?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa Tất cả") { dialog, which ->
                    mAdapter.data.clear()
                    mAdapter.notifyDataSetChanged()
                    mixRoomDatabase.getFavoriteDao().clearAllFavorite()
                    dialog.dismiss()
                    Snackbar.make(
                        binding.root, "Đã xóa tất cả.", Snackbar.LENGTH_SHORT
                    ).setTextColor(Color.parseColor("#ffa500")).show()
                    binding.tvEmpty.visibility = View.VISIBLE
                    clearFavorite!!.isVisible = false
                    searchFavorite!!.isVisible = false
                }
                .create()

            dialog.window?.apply {
                setBackgroundDrawableResource(R.color.teal_700)
            }
            dialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onRemoveToFavorite(favorite: Favorite) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_delete)
            .setTitle("Xóa Video")
            .setMessage("Bạn có muốn xóa video này ra khỏi danh sách yêu thích hay không ?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { dialog, which ->
                mAdapter.data.remove(favorite)
                mAdapter.notifyDataSetChanged()
                mixRoomDatabase.getFavoriteDao().deleteFavoriteVideo(favorite)
                dialog.dismiss()
                Snackbar.make(
                    binding.root, "Đã xóa video.", Snackbar.LENGTH_SHORT
                ).setTextColor(Color.parseColor("#ffa500")).show()
                if (mAdapter.data.isNullOrEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    clearFavorite!!.isVisible = false
                    searchFavorite!!.isVisible = false
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    clearFavorite!!.isVisible = true
                    searchFavorite!!.isVisible = true
                }
            }
            .create()

        dialog.window?.apply {
            setBackgroundDrawableResource(R.color.teal_700)
        }
        dialog.show()
    }
}