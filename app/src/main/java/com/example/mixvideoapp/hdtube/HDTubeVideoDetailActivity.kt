package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityHdtubeVideoDetailBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class HDTubeVideoDetailActivity : AppCompatActivity(), HDTubeVideoAdapter.HDTubeVideoListener,
    HDTubeModelAdapter.HDTubeModelListener {

    private lateinit var binding: ActivityHdtubeVideoDetailBinding
    private lateinit var hdTubeVideo: HDTubeVideo
    private lateinit var hdTubeInfoDetail: HDTubeInfoDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHdtubeVideoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hdTubeVideo = intent.getSerializableExtra("HDTubeVideo") as HDTubeVideo
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolBar.setNavigationOnClickListener { onBackPressed() }

        binding.tvTitle.text = hdTubeVideo.title
        binding.tvDuration.text = hdTubeVideo.duration
        Glide.with(this)
            .load(hdTubeVideo.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.ivThumb)

        loadData()
        binding.ivPlayTrailer.setOnClickListener { playTrailer() }

    }

    private fun playTrailer() {
        binding.contCover.visibility = View.GONE
        binding.btnPlay.visibility = View.GONE
        binding.vvTrailer.setVideoURI(Uri.parse(hdTubeVideo.trailer))
        binding.vvTrailer.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
            if (mp.isPlaying) {
                binding.pbTrailer.visibility = View.GONE
            } else {
                binding.pbTrailer.visibility = View.VISIBLE
            }
        }
        binding.vvTrailer.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            binding.contCover.visibility = View.VISIBLE
            binding.btnPlay.visibility = View.VISIBLE
            Toast.makeText(this, "video trailer error", Toast.LENGTH_SHORT).show()
            true
        }
        binding.vvTrailer.setOnCompletionListener { mp: MediaPlayer? ->
            binding.vvTrailer.stopPlayback()
            binding.contCover.visibility = View.VISIBLE
            binding.btnPlay.visibility = View.VISIBLE
        }
//        binding.vvTrailer.start()
    }

    private fun loadData() {
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlVideosByUrl(hdTubeVideo.href)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {

                override fun onNext(html: String) {
                    hdTubeInfoDetail = ParseHDTube.parseInfodetail(html)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(this@HDTubeVideoDetailActivity, "Error", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("TAG", "Load more error: ${e.message}")
                    if (e.message!!.contains("HTTP 404")) {
                    }
                }

                override fun onComplete() {
                    showInfoDetail()
                    binding.btnPlay.visibility = View.VISIBLE
                }
            })
    }

    private fun showInfoDetail() {
        binding.tvViews.text = hdTubeInfoDetail.views
        binding.tvDate.text = hdTubeInfoDetail.date
        binding.tvDescription.text = hdTubeInfoDetail.desciption

        val tagAdapter = HDTubeTagAdapter(
            R.layout.item_tag_hdtube_horizontal, hdTubeInfoDetail.listTags
        )
        tagAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT)
        binding.rvTag.setHasFixedSize(true)
        binding.rvTag.adapter = tagAdapter

        val videoAdapter = HDTubeVideoAdapter(
            R.layout.item_video_hdtube, hdTubeInfoDetail.listRelated, this
        )
        videoAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvVideo.setHasFixedSize(true)
        binding.rvVideo.adapter = videoAdapter

        val modelAdapter = HDTubeModelAdapter(
            R.layout.item_model_hdtube_small, hdTubeInfoDetail.listModels, this
        )
        modelAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT)
        binding.rvModel.setHasFixedSize(true)
        binding.rvModel.adapter = modelAdapter

        binding.btnPlay.setOnClickListener {
            val intent = Intent(this, HDTubePlayerActivity::class.java)
            intent.putExtra("title", hdTubeVideo.title)
            intent.putExtra("source", hdTubeInfoDetail.source)
            startActivity(intent)
        }
    }

    override fun onVideoClick(hdTubeVideo: HDTubeVideo) {
        val intent = intent
        intent.putExtra("HDTubeVideo", hdTubeVideo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        finish()
        startActivity(intent)
    }

    override fun onModelClick(hdTubeModel: HDTubeModel) {
        val intent = Intent(this, HDTubeModelDetailActivity::class.java)
        intent.putExtra("HDTubeModel", hdTubeModel)
        startActivity(intent)
    }

}