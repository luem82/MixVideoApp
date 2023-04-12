package com.example.mixvideoapp.xcuto

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.model.MixVideo

class VertiAdapter(
    layoutResId: Int, data: List<MixVideo>?,
    private val listener: VertiVideoListener
) : BaseQuickAdapter<MixVideo, BaseViewHolder>(layoutResId, data) {

    private var mVideoViewPlaying: VideoView? = null

    override fun convert(helper: BaseViewHolder, item: MixVideo) {
        helper.getView<ImageView>(R.id.favorite_video_remove).visibility=View.GONE
        helper.setText(R.id.favorite_video_title, item.title)
        helper.setText(R.id.favorite_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(120, 90)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.favorite_video_thumb))
        val videoView = helper.getView<VideoView>(R.id.favorite_vv_preview)
        val loading = helper.getView<ProgressBar>(R.id.favorite_pb_load)
        if (helper.adapterPosition == helper.oldPosition) {
            videoView.visibility = View.VISIBLE
        } else {
            videoView.visibility = View.GONE
            loading.visibility = View.GONE
        }

        helper.setOnClickListener(R.id.favorite_video_preview) { v: View? ->
            playPreviewVideo(item, videoView, loading)
        }

        helper.setOnClickListener(R.id.favorite_video_play) { v: View? ->
            listener.onPlayVideo(item)
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

    interface VertiVideoListener{
        fun onPlayVideo(mixVideo: MixVideo)
    }
}