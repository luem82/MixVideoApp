package com.example.mixvideoapp.allxinfo

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class AllXVideoAdapter(
    layoutResId: Int, data: List<AllXVideo>?, val iVideoListener: IVideoListener
) : BaseQuickAdapter<AllXVideo, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: AllXVideo) {
        helper.setText(R.id.allx_video_title, item.title)
        helper.setText(R.id.allx_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(240, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.allx_video_thumb))

        helper.setOnClickListener(R.id.card_video_allxinfo) { v: View? ->
            iVideoListener.onVideoClick(item)
        }
    }

    interface IVideoListener {
        fun onVideoClick(allXVideo: AllXVideo)
    }
}
