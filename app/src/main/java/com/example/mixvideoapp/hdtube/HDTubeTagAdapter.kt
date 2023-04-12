package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class HDTubeTagAdapter(
    layoutResId: Int, data: List<String>?
) : BaseQuickAdapter<String, BaseViewHolder>(layoutResId, data) {

    override fun convert(helper: BaseViewHolder, item: String) {
        if (mLayoutResId == R.layout.item_tag_hdtube_vertical) {
            helper.setText(R.id.tv_tag_name, item)
            helper.setOnClickListener(R.id.tag_vertical, { v: View? ->
                val intent = Intent(mContext, HDTubeTagActivity::class.java)
                intent.putExtra("tag_name", item)
                mContext.startActivity(intent)
            })
        } else {
            helper.setText(R.id.tv_tag_name, item)
            helper.setOnClickListener(R.id.tag_horizontal, { v: View? ->
                val intent = Intent(mContext, HDTubeTagActivity::class.java)
                intent.putExtra("tag_name", item)
                mContext.startActivity(intent)
            })
        }
    }
}