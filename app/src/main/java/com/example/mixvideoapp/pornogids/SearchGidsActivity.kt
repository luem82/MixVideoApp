package com.example.mixvideoapp.pornogids

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivitySearchGidsBinding

class SearchGidsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchGidsBinding
    private lateinit var mQuery: String
    private lateinit var mTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchGidsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mQuery = intent.getStringExtra("key").toString()
        mTitle = intent.getStringExtra("title").toString()
        setUpToolbar()
        setUpVideos()
    }

    private fun setUpVideos() {
        supportFragmentManager.beginTransaction().replace(
            R.id.cont_search_gids,
            GidsVideoListFragment.newInstance("search", null, null, mQuery)
        ).disallowAddToBackStack().commit()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = "Search result: ${mTitle}"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}