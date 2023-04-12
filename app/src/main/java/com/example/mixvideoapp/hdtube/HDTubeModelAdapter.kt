package com.example.mixvideoapp.hdtube

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class HDTubeModelAdapter(
    layoutResId: Int, data: List<HDTubeModel>?,
    private val hdTubeModelListener: HDTubeModelListener
) : BaseQuickAdapter<HDTubeModel, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: HDTubeModel) {
        if (mLayoutResId == R.layout.item_model_hdtube_big) {
            helper.setText(R.id.model_name_big, item.name)
            Glide.with(mContext)
                .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.getView(R.id.model_thumb_big))

            helper.setOnClickListener(R.id.card_model_big) { v: View? ->
                hdTubeModelListener.onModelClick(item)
            }
        } else {
            helper.setText(R.id.model_name_small, item.name)
            Glide.with(mContext)
                .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.getView(R.id.model_thumb_small))

            helper.setOnClickListener(R.id.card_model_small) { v: View? ->
                hdTubeModelListener.onModelClick(item)
            }
        }

    }

    interface HDTubeModelListener {
        fun onModelClick(hdTubeModel: HDTubeModel)
    }
}