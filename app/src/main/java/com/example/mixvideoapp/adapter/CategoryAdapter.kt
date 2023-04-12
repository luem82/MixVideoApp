package com.example.mixvideoapp.adapter

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.CategoryActivity
import com.example.mixvideoapp.model.MixCategory

class CategoryAdapter(
    layoutResId: Int, data: List<MixCategory>?
) : BaseQuickAdapter<MixCategory, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: MixCategory) {
        helper.setText(R.id.cate_title, item.title)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(160, 120)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.cate_thumb))

        helper.setOnClickListener(R.id.card_category, { v: View? ->
            val intent = Intent(mContext, CategoryActivity::class.java)
            intent.putExtra("MixCategory", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        })
    }
}