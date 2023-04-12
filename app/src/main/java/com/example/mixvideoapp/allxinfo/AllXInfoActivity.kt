package com.example.mixvideoapp.allxinfo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityAllXinfoBinding
import java.util.*

class AllXInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllXinfoBinding

    companion object {
        val ALLX_URL = "https://allx.vip"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllXinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewToolBar()
        initViewPager()
        initChipGroup()
    }

    private fun initViewToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initViewPager() {
        val listFragment = ArrayList<Fragment>()
        listFragment.add(ListVideoAllXInfoFragment.newInstance(ALLX_URL + "/moi-cap-nhat/"))
        listFragment.add(ListVideoAllXInfoFragment.newInstance(ALLX_URL + "/pho-bien/"))
        listFragment.add(ListVideoAllXInfoFragment.newInstance(ALLX_URL + "/noi-bat/"))
        listFragment.add(AllXCategoryFragment.newInstance(ALLX_URL + "/the-loai/"))
        val viewPagerAdapter = AllXInfoPagerAdapter(listFragment, supportFragmentManager, lifecycle)
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.offscreenPageLimit = 3

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.chiNew.isCheckable = true
                        binding.chiNew.isChecked = true
                        binding.moreHoriScroll.scrollX = binding.chiNew.x.toInt()
                    }
                    1 -> {
                        binding.chipPopular.isCheckable = true
                        binding.chipPopular.isChecked = true
                        binding.moreHoriScroll.scrollX = binding.chipPopular.x.toInt()
                    }
                    2 -> {
                        binding.chipRated.isCheckable = true
                        binding.chipRated.isChecked = true
                        binding.moreHoriScroll.scrollX = binding.chipRated.x.toInt()
                    }
                    else -> {
                        binding.chipCategory.isCheckable = true
                        binding.chipCategory.isChecked = true
                        binding.moreHoriScroll.scrollX = binding.chipCategory.x.toInt()
                    }
                }
            }
        })
    }

    private fun initChipGroup() {
        binding.chiNew.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.chipPopular.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.chipRated.setOnClickListener { binding.viewPager.currentItem = 2 }
        binding.chipCategory.setOnClickListener { binding.viewPager.currentItem = 3 }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_allxinfo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.act_search_allxinfo) {
            startActivity(Intent(this, SearchAllXInfoActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}