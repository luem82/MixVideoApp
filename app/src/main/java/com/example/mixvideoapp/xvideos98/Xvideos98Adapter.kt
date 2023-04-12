package com.example.mixvideoapp.xvideos98

import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.VideoView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class Xvideos98Adapter(
    layoutResId: Int, data: List<Xvideos98>?, private val iXvideos98Listener: IXvideos98Listener
) : BaseQuickAdapter<Xvideos98, BaseViewHolder>(layoutResId, data) {

    interface IXvideos98Listener {
        fun onClickXvideos98(xvideos98: Xvideos98)
    }

    private var mVideoViewPlaying: VideoView? = null

    override fun convert(helper: BaseViewHolder, item: Xvideos98) {

        if (mLayoutResId == R.layout.item_video_x98) {
            helper.setText(R.id.x98_video_title, item.title)
            helper.setText(R.id.x98_video_duration, item.duration)
            helper.setText(R.id.x98_video_views, item.views)
            helper.setText(R.id.x98_video_rates, item.rates)
            Glide.with(mContext)
                .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(helper.getView(R.id.x98_video_thumb))

            val videoView = helper.getView<VideoView>(R.id.x98_vv_preview)
            val loading = helper.getView<ProgressBar>(R.id.x98_pb_load)
            if (helper.adapterPosition == helper.oldPosition) {
                videoView.visibility = View.VISIBLE
            } else {
                videoView.visibility = View.GONE
                loading.visibility = View.GONE
            }

            helper.setOnLongClickListener(R.id.x98_video_item) {
                playPreviewVideo(item, videoView, loading)
                true
            }

            helper.setOnClickListener(R.id.x98_video_item) { v: View? ->
                iXvideos98Listener.onClickXvideos98(item)
            }
        } else if (mLayoutResId == R.layout.item_video_more) {
            helper.setText(R.id.title_more_x98, item.title)
            helper.setText(R.id.duration_more_x98, item.duration)
            Glide.with(mContext)
                .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(helper.getView(R.id.thumb_more_x98))

            helper.setOnClickListener(R.id.item_more_x98) { v: View? ->
                iXvideos98Listener.onClickXvideos98(item)
            }
        }

    }

    private fun playPreviewVideo(item: Xvideos98, videoView: VideoView, loading: ProgressBar) {
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