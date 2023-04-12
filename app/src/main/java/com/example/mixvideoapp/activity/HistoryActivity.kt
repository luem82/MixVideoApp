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
import com.example.mixvideoapp.adapter.HistoryAdapter
import com.example.mixvideoapp.databinding.ActivityHistoryBinding
import com.example.mixvideoapp.interfaces.IVideoHistoryListener
import com.example.mixvideoapp.room.History
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity(), IVideoHistoryListener {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var mAdapter: HistoryAdapter
    private var clearHistory: MenuItem? = null
    private var searchHistory: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
        initAdapter()
    }

    private fun initAdapter() {
        val data = mixRoomDatabase.getHistoryDao().getHistoryVideos()
        mAdapter = HistoryAdapter(R.layout.item_video_history, data, this, this)
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
        menuInflater.inflate(R.menu.menu_history, menu)
        clearHistory = menu?.findItem(R.id.act_clear_history)
        searchHistory = menu?.findItem(R.id.act_search_history)
        if (mAdapter.data.isNullOrEmpty()) {
            clearHistory?.isVisible = false
            searchHistory?.isVisible = false
        } else {
            clearHistory?.isVisible = true
            searchHistory?.isVisible = true
        }

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        var searchView: SearchView? = SearchView(this)

        searchHistory?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView!!.queryHint = resources.getString(R.string.str_search_history)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView!!.queryHint = ""
                return true
            }
        })

        if (searchHistory != null) {
            searchView = searchHistory!!.actionView as SearchView
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
        if (item.itemId == R.id.act_clear_history) {
            val dialog = MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_clear_history)
                .setTitle("Xóa Tất Cả")
                .setMessage("Bạn có muốn xóa tất cả lịch sử đã xem hay không ?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa Tất cả") { dialog, which ->
                    mAdapter.data.clear()
                    mAdapter.notifyDataSetChanged()
                    mixRoomDatabase.getHistoryDao().clearAllHistory()
                    dialog.dismiss()
                    Snackbar.make(
                        binding.root, "Đã xóa tất cả.", Snackbar.LENGTH_SHORT
                    ).setTextColor(Color.parseColor("#ffa500")).show()
                    binding.tvEmpty.visibility = View.VISIBLE
                    searchHistory?.isVisible = false
                    clearHistory?.isVisible = false
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

    override fun onRemoveToHistory(history: History) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_delete)
            .setTitle("Xóa Video")
            .setMessage("Bạn có muốn xóa video này ra khỏi lịch sử đã xem hay không ?")
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { dialog, which ->
                mAdapter.data.remove(history)
                mAdapter.notifyDataSetChanged()
                mixRoomDatabase.getHistoryDao().deleteHistoryVideo(history)
                dialog.dismiss()
                Snackbar.make(
                    binding.root, "Đã xóa video.", Snackbar.LENGTH_SHORT
                ).setTextColor(Color.parseColor("#ffa500")).show()
                if (mAdapter.data.isNullOrEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    clearHistory?.isVisible = false
                    searchHistory?.isVisible = false
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    clearHistory?.isVisible = true
                    searchHistory?.isVisible = true
                }
            }
            .create()

        dialog.window?.apply {
            setBackgroundDrawableResource(R.color.teal_700)
        }
        dialog.show()
    }
}