package com.example.mixvideoapp.pornogids

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityPornogidsBinding
import java.util.*

class PornogidsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPornogidsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPornogidsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolbar()
        setUpViewPager()
    }

    private fun setUpViewPager() {
        val listFragment = ArrayList<Fragment>()
        listFragment.add(GidsVideoListFragment.newInstance("first", "latest-updates", null, null))
//        listFragment.add(GidsVideoListFragment.newInstance("search",  null, null, "mom-and-son"))
//        listFragment.add(GidsVideoListFragment.newInstance("category",  null, "big-ass", null))
        listFragment.add(GidsCategoryListFragment.newInstance())
        val gidsPagerAdapter = GidsPagerAdapter(listFragment, supportFragmentManager, lifecycle)
        binding.viewPager.adapter = gidsPagerAdapter
        binding.viewPager.offscreenPageLimit = 1

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.botNav.menu.findItem(R.id.bot_gids_video).isChecked = true
                    }
                    else -> {
                        binding.botNav.menu.findItem(R.id.bot_gid_category).isChecked = true
                    }
                }
            }
        })

        binding.botNav.setOnItemSelectedListener {
            if (it.itemId == R.id.bot_gids_video) {
                binding.viewPager.currentItem = 0
                true
            } else {
                binding.viewPager.currentItem = 1
                true
            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pornogids, menu)
        if (binding.viewPager.currentItem == 0) {
            menu!!.findItem(R.id.gids_filter).isVisible = true
        } else {
            menu!!.findItem(R.id.gids_filter).isVisible = false
        }
        Log.e("gids_video", "onCreateOptionsMenu: ")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    class GidsPagerAdapter(
        private val fragmentArrayList: ArrayList<Fragment>,
        fragmentManager: FragmentManager?,
        lifecycle: Lifecycle?
    ) : FragmentStateAdapter(
        fragmentManager!!, lifecycle!!
    ) {
        override fun createFragment(position: Int): Fragment {
            return fragmentArrayList[position]
        }

        override fun getItemCount(): Int {
            return fragmentArrayList.size
        }
    }
}