package com.example.mixvideoapp.lesbian8

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.model.MixVideo

class Les8VideoAdapter(
    layoutResId: Int, data: List<MixVideo>?, context: Context?
) : BaseQuickAdapter<MixVideo, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: MixVideo) {
        helper.setText(R.id.les_video_title, item.title)
        helper.setText(R.id.les_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 180)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.les_video_thumb))

        helper.setOnClickListener(R.id.card_video_les) { v: View? ->
            val intent = Intent(mContext, Les8DetailVideoActivity::class.java)
            intent.putExtra("MixVideo", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }

    }
}