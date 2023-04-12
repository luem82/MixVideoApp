package com.example.mixvideoapp.activity

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mixvideoapp.R
import com.example.mixvideoapp.adapter.PhotoAdapter
import com.example.mixvideoapp.databinding.ActivityKenrPhotoBinding
import com.example.mixvideoapp.model.MixPhoto
import com.google.android.material.snackbar.Snackbar

class KenrPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKenrPhotoBinding
    private lateinit var list: List<MixPhoto>
    private lateinit var photoAdapter: PhotoAdapter
    private val mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable {
        val currentPosition = binding.vpSlideShow.currentItem
        if (currentPosition == list.size - 1) {
            val snackbar = Snackbar.make(
                binding.root, "Kết thúc danh sách", Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("Đóng") {
                snackbar.dismiss()
                finish()
            }
            snackbar.show()
        } else {
            binding.vpSlideShow.currentItem = currentPosition + 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityKenrPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        list = intent.getSerializableExtra("list") as List<MixPhoto>
        initUI()
    }

    private fun initUI() {
        //imersie mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowInsetsCompat = WindowInsetsControllerCompat(window, binding.root)
            windowInsetsCompat.addOnControllableInsetsChangedListener { controller: WindowInsetsControllerCompat, typeMask: Int ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }


        photoAdapter = PhotoAdapter(R.layout.item_photo_kenburns, list, null)
        binding.vpSlideShow.adapter = photoAdapter
//        binding.vpSlideShow.setPageTransformer(DepthPageTransformer())
        binding.vpSlideShow.offscreenPageLimit = list.size
        binding.vpSlideShow.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandler.removeCallbacks(mRunnable)
                mHandler.postDelayed(mRunnable, 5000)
                if (binding.btnStop.visibility == View.VISIBLE) {
                    binding.btnStop.visibility = View.GONE
                }
            }
        })
        binding.btnStop.setOnClickListener { v -> onBackPressed() }
    }


    fun showClose() {
        if (binding.btnStop.visibility == View.GONE) {
            binding.btnStop.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        mHandler.postDelayed(mRunnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(mRunnable)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}