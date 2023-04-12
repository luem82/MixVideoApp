package com.example.mixvideoapp.allxinfo

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class AllXCategoryAdapter(
    layoutResId: Int, data: List<AllXCategory>?
) : BaseQuickAdapter<AllXCategory, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: AllXCategory) {
        helper.setText(R.id.allx_cate_title, item.title)
        helper.setText(R.id.allx_cate_rates, item.rates)
        helper.setText(R.id.allx_cate_count, item.count)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.allx_cate_thumb))

        helper.setOnClickListener(R.id.card_category_allxinfo) { v: View? ->
            val intent = Intent(mContext, AllXCategoryDetailActivity::class.java)
            intent.putExtra("AllXCategory", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }

}
