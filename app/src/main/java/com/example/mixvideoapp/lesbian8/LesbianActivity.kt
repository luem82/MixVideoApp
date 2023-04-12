package com.example.mixvideoapp.lesbian8

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityLesbianBinding
import com.google.android.material.navigation.NavigationBarView

class LesbianActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    private lateinit var binding: ActivityLesbianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLesbianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        replaceFragment(Les8VideoFragment.newInstance())
        binding.botNav.setOnItemSelectedListener(this)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_les, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bot_les_video -> {
                replaceFragment(Les8VideoFragment.newInstance())
                return true
            }
            R.id.bot_les_category -> {
                replaceFragment(Les8CategoryFragment.newInstance())
                return true
            }
            else -> {
                replaceFragment(Les8AlbumFragment.newInstance())
                return true
            }
        }
    }

}