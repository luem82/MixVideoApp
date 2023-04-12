package com.example.mixvideoapp.hdtube

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class HDTubeVideoAdapter(
    layoutResId: Int, data: List<HDTubeVideo>?,
    private val hdTubeVideoListener: HDTubeVideoListener
) : BaseQuickAdapter<HDTubeVideo, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: HDTubeVideo) {
        helper.setText(R.id.hdtube_video_title, item.title)
        helper.setText(R.id.hdtube_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(160, 90)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.hdtube_video_thumb))

        helper.setOnClickListener(R.id.card_video_hdtube) { v: View? ->
            hdTubeVideoListener.onVideoClick(item)
        }

    }

    interface HDTubeVideoListener {
        fun onVideoClick(hdTubeVideo: HDTubeVideo)
    }
}