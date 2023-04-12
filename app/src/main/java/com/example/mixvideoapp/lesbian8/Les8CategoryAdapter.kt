package com.example.mixvideoapp.lesbian8

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.model.MixCategory

class Les8CategoryAdapter(
    layoutResId: Int, data: List<MixCategory>?
) : BaseQuickAdapter<MixCategory, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: MixCategory) {
        helper.setText(R.id.les_cate_title, item.title)
        helper.setText(R.id.les_cate_count, "${item.max} videos")
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(175, 215)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.les_cate_thumb))

        helper.setOnClickListener(R.id.card_category_les) { v: View? ->
            val intent = Intent(mContext, Les8DetailCategoryActivity::class.java)
            intent.putExtra("MixCategory", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }

    }
}