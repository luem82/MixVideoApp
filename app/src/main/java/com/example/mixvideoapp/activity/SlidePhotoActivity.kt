package com.example.mixvideoapp.activity

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.mixvideoapp.R
import com.example.mixvideoapp.adapter.PhotoAdapter
import com.example.mixvideoapp.databinding.ActivitySlidePhotoBinding
import com.example.mixvideoapp.model.MixPhoto
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.Serializable

class SlidePhotoActivity : AppCompatActivity(), PhotoAdapter.IHoriPhotoListener {

    private lateinit var binding: ActivitySlidePhotoBinding
    private var selectedPosition: Int = 0
    private lateinit var list: List<MixPhoto>
    private lateinit var slideAdapter: PhotoAdapter
    private lateinit var scrollAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySlidePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedPosition = intent.getIntExtra("position", 0)
        list = intent.getSerializableExtra("list") as List<MixPhoto>
        iniAdapter()
    }

    private fun iniAdapter() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "${selectedPosition + 1} / ${list.size}"
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        scrollAdapter = PhotoAdapter(R.layout.item_photo_scroll, list, this)
        slideAdapter = PhotoAdapter(R.layout.item_photo_slide, list, null)
        binding.rvScroll.adapter = scrollAdapter
        binding.vpSlide.adapter = slideAdapter
        binding.vpSlide.currentItem = selectedPosition
//        binding.vpSlide.offscreenPageLimit = list.size
//        binding.vpSlide.setPageTransformer(DepthPageTransformer())
        binding.vpSlide.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                scrollAdapter.handlerItemClick(position)
                binding.rvScroll.smoothScrollToPosition(position)
                supportActionBar?.title = "${position + 1} / ${list.size}"
            }
        })
        scrollAdapter.handlerItemClick(selectedPosition)
        binding.rvScroll.smoothScrollToPosition(selectedPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_photo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.act_photo_download) {
            downloadPhoto()
        } else if (item.itemId == R.id.act_photo_slide) {
            autoSlide()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun autoSlide() {
        val intent = Intent(this, KenrPhotoActivity::class.java)
        intent.putExtra("list", list as Serializable)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun downloadPhoto() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    val url = list[binding.vpSlide.currentItem].big
                    val fileName = "TV-69_${System.currentTimeMillis()}.jpg"
                    val desciption = "Hình Ảnh TV-69"
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setTitle(fileName)
                    request.setDescription(desciption)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, fileName
                    )
                    request.allowScanningByMediaScanner()
                    request.setAllowedOverMetered(true)
                    request.setAllowedOverRoaming(false)

                    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    manager.enqueue(request)
                    Snackbar.make(
                        binding.root, "Đang tải xuống ${desciption}.", Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    ActivityCompat.startActivityForResult(
                        this@SlidePhotoActivity, intent, 100, null
                    )
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?, token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            }).check()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onHoriItemClick(position: Int) {
        binding.vpSlide.setCurrentItem(position, true)
        supportActionBar?.title = "${position + 1} / ${list.size}"
    }
}