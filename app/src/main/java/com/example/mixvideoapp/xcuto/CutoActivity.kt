package com.example.mixvideoapp.xcuto

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityCutoBinding
import com.google.android.material.navigation.NavigationBarView

class CutoActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityCutoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        replaceFragment(CutoFragment.newInstance())
        binding.botNav.setOnItemSelectedListener(this)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_cuto, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bot_cuto_video -> {
                replaceFragment(CutoFragment.newInstance())
                return true
            }
            R.id.bot_cuto_category -> {
                replaceFragment(CategoryCutoFragment.newInstance())
                return true
            }
            else -> {
                replaceFragment(SearchCutoFragment.newInstance())
                return true
            }
        }
    }

}