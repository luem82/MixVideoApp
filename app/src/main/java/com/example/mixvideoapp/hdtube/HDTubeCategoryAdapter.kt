package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class HDTubeCategoryAdapter(
    layoutResId: Int, data: List<HDTubeCategory>?
) : BaseQuickAdapter<HDTubeCategory, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: HDTubeCategory) {
        helper.setText(R.id.cate_title, item.title)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(160, 90)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.cate_thumb))

        helper.setOnClickListener(R.id.card_category, { v: View? ->
            val intent = Intent(mContext, HDTubeCategoryActivity::class.java)
            intent.putExtra("HDTubeCategory", item)
            mContext.startActivity(intent)
        })
    }
}