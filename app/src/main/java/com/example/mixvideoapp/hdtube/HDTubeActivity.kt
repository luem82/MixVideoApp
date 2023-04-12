package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityHdtubeBinding
import java.util.*

class HDTubeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHdtubeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHdtubeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        val listFragment = ArrayList<Fragment>()
        listFragment.add(HDTubeVideosFragment.newInstance())
        listFragment.add(HDTubeCategoriesFragment.newInstance())
        listFragment.add(HDTubeTagsFragment.newInstance())
        listFragment.add(HDTubeModelFragment.newInstance())
        val viewPagerAdapter = HDTubePagerAdapter(listFragment, supportFragmentManager, lifecycle)
        binding.viewPagerHdtube.adapter = viewPagerAdapter
        binding.viewPagerHdtube.offscreenPageLimit = 3

        binding.viewPagerHdtube.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.botNav.menu.findItem(R.id.bot_hdtube_video).isChecked = true
                    }
                    1 -> {
                        binding.botNav.menu.findItem(R.id.bot_hdtube_category).isChecked = true
                    }
                    2 -> {
                        binding.botNav.menu.findItem(R.id.bot_hdtube_tag).isChecked = true
                    }
                    else -> {
                        binding.botNav.menu.findItem(R.id.bot_hdtube_model).isChecked = true
                    }
                }
            }
        })

        binding.botNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bot_hdtube_video -> {
                    binding.viewPagerHdtube.currentItem = 0
                    true
                }
                R.id.bot_hdtube_category -> {
                    binding.viewPagerHdtube.currentItem = 1
                    true
                }
                R.id.bot_hdtube_tag -> {
                    binding.viewPagerHdtube.currentItem = 2
                    true
                }
                R.id.bot_hdtube_model -> {
                    binding.viewPagerHdtube.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_allxinfo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.act_search_allxinfo) {
            startActivity(Intent(this, SearchHDTubeActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}