package com.example.mixvideoapp.allxinfo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class AllXInfoPagerAdapter(
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