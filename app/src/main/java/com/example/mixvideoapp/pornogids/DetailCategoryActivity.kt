package com.example.mixvideoapp.pornogids

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityDetailCategoryBinding

class DetailCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailCategoryBinding
    private lateinit var gidsCategory: GidsCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gidsCategory = intent.getSerializableExtra("GidsCategory") as GidsCategory
        initCollapsingToolbar()
        setUpVideos()
    }

    private fun setUpVideos() {
        supportFragmentManager.beginTransaction().replace(
            R.id.cont_cate_video,
            GidsVideoListFragment.newInstance("catagory", null, gidsCategory.key, null)
        ).disallowAddToBackStack().commit()
    }

    private fun initCollapsingToolbar() {
        Glide.with(this)
            .load(gidsCategory.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
//            .override(175, 215)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.ivHeader)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = gidsCategory.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}