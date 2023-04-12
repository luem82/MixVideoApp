package com.example.mixvideoapp.xcuto

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.model.MixVideo

class CutoVideoAdapter(
    layoutResId: Int, data: List<MixVideo>?, context: Context?
) : BaseQuickAdapter<MixVideo, BaseViewHolder>(layoutResId, data) {

    private var mVideoViewPlaying: VideoView? = null

    override fun convert(helper: BaseViewHolder, item: MixVideo) {
        helper.setText(R.id.big_video_title, item.title)
        helper.setText(R.id.big_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.big_video_thumb))
        val videoView = helper.getView<VideoView>(R.id.big_vv_preview)
        val loading = helper.getView<ProgressBar>(R.id.big_pb_load)
        if (helper.adapterPosition == helper.oldPosition) {
            videoView.visibility = View.VISIBLE
        } else {
            videoView.visibility = View.GONE
            loading.visibility = View.GONE
        }

        helper.setOnClickListener(R.id.big_video_preview) { v: View? ->
            playPreviewVideo(item, videoView, loading)
        }

        helper.setOnClickListener(R.id.big_video_play) { v: View? ->
//            val intent = Intent(mContext, WebViewActivity::class.java)
//            intent.putExtra("embed_url", item.href)
//            intent.putExtra("video_title", item.title)
//            mContext.startActivity(intent)
//            (mContext as AppCompatActivity).overridePendingTransition(
//                android.R.anim.fade_in, android.R.anim.fade_out
//            )

            val intent = Intent(mContext, WebExoActivity::class.java)
            intent.putExtra("MixVideo", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }

    }

    private fun playPreviewVideo(item: MixVideo, videoView: VideoView, loading: ProgressBar) {
        if (item.preview == null) {
            return
        }
        if (mVideoViewPlaying != null) {
            mVideoViewPlaying!!.stopPlayback()
            mVideoViewPlaying!!.visibility = View.GONE

            loading.visibility = View.GONE
        }
        mVideoViewPlaying = videoView
        videoView.visibility = View.VISIBLE
        loading.visibility = View.VISIBLE
        videoView.setVideoURI(Uri.parse(item.preview))
        videoView.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
            if (mp.isPlaying) {
                loading.visibility = View.GONE
            } else {
                loading.visibility = View.VISIBLE
            }
        }
        videoView.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            loading.visibility = View.GONE
            mVideoViewPlaying!!.visibility = View.GONE
            videoView.visibility = View.GONE
            Toast.makeText(mContext, "video error", Toast.LENGTH_SHORT).show()
            true
        }
        videoView.setOnCompletionListener { mp: MediaPlayer? ->
            mVideoViewPlaying!!.stopPlayback()
            mVideoViewPlaying!!.visibility = View.GONE
            mVideoViewPlaying = null
            loading.visibility = View.GONE
        }
        videoView.start()
    }
}