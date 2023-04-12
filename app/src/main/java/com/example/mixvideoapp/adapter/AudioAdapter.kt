package com.example.mixvideoapp.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.AudioPlayerActivity
import com.example.mixvideoapp.model.MixAudio

class AudioAdapter(
    layoutResId: Int, data: List<MixAudio>?, context: Context?
) : BaseQuickAdapter<MixAudio, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: MixAudio) {
        helper.setText(R.id.big_audio_title, item.title)
        helper.setText(R.id.big_audio_views, item.views)
        helper.setText(R.id.big_audio_date, item.date)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(320, 240)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.big_audio_thumb))

        helper.setOnClickListener(R.id.big_audio_play) { v: View? ->
            val intent = Intent(mContext, AudioPlayerActivity::class.java)
            intent.putExtra("MixAudio", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }

    }
}