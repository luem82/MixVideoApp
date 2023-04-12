package com.example.mixvideoapp.adapter

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.DetailAlbumActivity
import com.example.mixvideoapp.model.MixAlbum

class AlbumAdapter(
    layoutResId: Int, data: List<MixAlbum>?
) : BaseQuickAdapter<MixAlbum, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: MixAlbum) {
        helper.setText(R.id.album_title, item.title)
        helper.setText(R.id.album_count, item.count)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.album_thumb))

        helper.setOnClickListener(R.id.album_card) { v: View? ->
            val intent = Intent(mContext, DetailAlbumActivity::class.java)
            intent.putExtra("MixAlbum", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }
}