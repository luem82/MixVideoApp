package com.example.mixvideoapp.pornogids

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class GidsVideoAdapter(
    layoutResId: Int, data: List<GidsVideo>?,
    private val iGidsVideoListener: IGidsVideoListener
) : BaseQuickAdapter<GidsVideo, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: GidsVideo) {
        helper.setText(R.id.gids_video_title, item.title)
        helper.setText(R.id.gids_video_duration, item.duration)
        helper.setText(R.id.gids_video_rates, item.rates)
        helper.setText(R.id.gids_video_views, item.views)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .into(helper.getView(R.id.gids_video_thumb))

        helper.setOnClickListener(R.id.item_gids) { v: View? ->
            iGidsVideoListener.onGidsVideoClick(item)
        }

    }

    interface IGidsVideoListener {
        fun onGidsVideoClick(gidsVideo: GidsVideo)
    }
}