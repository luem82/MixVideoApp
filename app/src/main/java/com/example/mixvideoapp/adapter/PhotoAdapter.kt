package com.example.mixvideoapp.adapter

import android.content.Intent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.KenrPhotoActivity
import com.example.mixvideoapp.activity.SlidePhotoActivity
import com.example.mixvideoapp.model.MixPhoto
import com.flaviofaria.kenburnsview.KenBurnsView
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import java.io.Serializable

class PhotoAdapter(
    layoutResId: Int, data: List<MixPhoto>?,
    private val iHoriPhotoListener: IHoriPhotoListener?
) : BaseQuickAdapter<MixPhoto, BaseViewHolder>(layoutResId, data) {

    private var indexPosition = 0

    override fun convert(helper: BaseViewHolder, item: MixPhoto) {
        if (mLayoutResId == R.layout.item_photo_grid) {
            Glide.with(mContext)
                .load(item.small)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.getView(R.id.grid_thumb))

            helper.setOnClickListener(R.id.grid_photo) { v: View? ->
                val intent = Intent(mContext, SlidePhotoActivity::class.java)
                intent.putExtra("list", data as Serializable)
                intent.putExtra("position", helper.absoluteAdapterPosition)
                mContext.startActivity(intent)
                (mContext as AppCompatActivity).overridePendingTransition(
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
            }

        } else if (mLayoutResId == R.layout.item_photo_slide) {
            Glide.with(mContext)
                .load(item.big)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.getView(R.id.slide_thumb))
        } else if (mLayoutResId == R.layout.item_photo_scroll) {
            Glide.with(mContext)
                .load(item.small)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.getView(R.id.scroll_thumb))
            helper.setText(R.id.scroll_pos, "${helper.absoluteAdapterPosition + 1}")

            if (indexPosition == helper.absoluteAdapterPosition) {
                //show indicator
                helper.setVisible(R.id.scroll_pos, true)
            } else {
                //hide indicator
                helper.setVisible(R.id.scroll_pos, false)
            }

            helper.setOnClickListener(R.id.scroll_photo) { v: View? ->
                handlerItemClick(helper.absoluteAdapterPosition)
            }
        } else {
            Glide.with(mContext)
                .load(item.big)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(Target.SIZE_ORIGINAL)
                .placeholder(R.drawable.loading)
                .error(R.drawable.no_pic)
                .into(helper.getView(R.id.kenburn_cover))

            val adi = AccelerateDecelerateInterpolator()
            val generator = RandomTransitionGenerator(4000, adi)
            helper.getView<KenBurnsView>(R.id.kenburn_cover).setTransitionGenerator(generator)

            helper.setOnClickListener(R.id.kenr_photo) { v: View? ->
                (mContext as KenrPhotoActivity).showClose()
            }
        }


    }

    interface IHoriPhotoListener {
        fun onHoriItemClick(position: Int)
    }

    fun handlerItemClick(position: Int) {
        if (this.iHoriPhotoListener != null) {
            indexPosition = position
            iHoriPhotoListener!!.onHoriItemClick(position)
            notifyDataSetChanged()
        }
    }
}