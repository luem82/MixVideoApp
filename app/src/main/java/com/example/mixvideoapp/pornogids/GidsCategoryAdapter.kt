package com.example.mixvideoapp.pornogids

import android.content.Intent
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R

class GidsCategoryAdapter(
    layoutResId: Int, data: List<GidsCategory>?
) : BaseQuickAdapter<GidsCategory, BaseViewHolder>(layoutResId, data), Filterable {

    var listFiltered: MutableList<GidsCategory>? = null

    init {
        this.listFiltered = mData
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val query = charSequence.toString()

                if (query.isEmpty()) {
                    mData = listFiltered!!
                } else {
                    var tempo = arrayListOf<GidsCategory>()

                    for (item in listFiltered!!) {
                        if (item.title.lowercase()!!.contains(query.lowercase())
                        ) {
                            tempo.add(item)
                        }
                    }
                    mData = tempo
                }

                val results = FilterResults()
                results.count = listFiltered!!.size
                results.values = listFiltered
                return results
            }

            override fun publishResults(charSequence: CharSequence, results: FilterResults) {
                listFiltered = results.values as MutableList<GidsCategory>
                notifyDataSetChanged()
            }
        }

    }

    override fun convert(helper: BaseViewHolder, item: GidsCategory) {
        helper.setText(R.id.gids_cate_title, item.title)
        helper.setText(R.id.gids_cate_rates, item.rates)
        helper.setText(R.id.gids_cate_count, item.count)

        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(160, 90)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(helper.getView(R.id.gids_cate_thumb))

        helper.setOnClickListener(R.id.card_category_gids) { v: View? ->
            val intent = Intent(mContext, DetailCategoryActivity::class.java)
            intent.putExtra("GidsCategory", item)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }
}





