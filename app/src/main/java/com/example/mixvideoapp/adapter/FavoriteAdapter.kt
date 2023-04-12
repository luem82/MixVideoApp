package com.example.mixvideoapp.adapter

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.mixvideoapp.R
import com.example.mixvideoapp.activity.VideoPlayerActivity
import com.example.mixvideoapp.interfaces.IVideoFavoriteListener
import com.example.mixvideoapp.room.Favorite

class FavoriteAdapter(
    layoutResId: Int, data: List<Favorite>?, context: Context?,
    private val listener: IVideoFavoriteListener
) : BaseQuickAdapter<Favorite, BaseViewHolder>(layoutResId, data), Filterable {

    private var mVideoViewPlaying: VideoView? = null
    var listFiltered: MutableList<Favorite>? = null


    init {
        this.listFiltered = mData
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {
                val query = charSequence.toString()

                if (query.isEmpty()) {
                    mData = listFiltered!!
                } else {
                    var tempo = arrayListOf<Favorite>()

                    for (item in listFiltered!!) {
                        if (item.title.lowercase()!!.contains(query.lowercase())) {
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
                listFiltered = results.values as MutableList<Favorite>
                notifyDataSetChanged()
            }
        }

    }

    override fun convert(helper: BaseViewHolder, item: Favorite) {
        helper.setText(R.id.favorite_video_title, item.title)
        helper.setText(R.id.favorite_video_duration, item.duration)
        Glide.with(mContext)
            .load(item.thumb)
//            .load("https://znews-photo.zingcdn.me/w1000/Uploaded/bfjysesfzyr/2022_10_10/xang_viet_Lin_11_.jpg")
            .override(120, 90)
            .placeholder(R.drawable.loading)
            .error(R.drawable.no_pic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(helper.getView(R.id.favorite_video_thumb))
        val videoView = helper.getView<VideoView>(R.id.favorite_vv_preview)
        val loading = helper.getView<ProgressBar>(R.id.favorite_pb_load)
        if (helper.adapterPosition == helper.oldPosition) {
            videoView.visibility = View.VISIBLE
        } else {
            videoView.visibility = View.GONE
            loading.visibility = View.GONE
        }

        helper.setOnClickListener(R.id.favorite_video_preview) { v: View? ->
            playPreviewVideo(item, videoView, loading)
        }

        helper.setOnClickListener(R.id.favorite_video_remove) { v: View? ->
            listener.onRemoveToFavorite(item)
        }

        helper.setOnClickListener(R.id.favorite_video_play) { v: View? ->
            val intent = Intent(mContext, VideoPlayerActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("thumb", item.thumb)
            intent.putExtra("href", item.href)
            intent.putExtra("duration", item.duration)
            intent.putExtra("preview", item.preview)
            intent.putExtra("type", item.type)
            intent.putExtra("favorite", item.favorite)
            intent.putExtra("history", false)
            intent.putExtra("current", 0)
            intent.putExtra("percent", 0)
            mContext.startActivity(intent)
            (mContext as AppCompatActivity).overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }

    private fun playPreviewVideo(item: Favorite, videoView: VideoView, loading: ProgressBar) {
        if (item.preview == null) {
            return
        }
        if (mVideoViewPlaying != null) {
            mVideoViewPlaying!!.stopPlayback()
            mVideoViewPlaying!!.visibility = View.GONE

            loading.visibility = View.GONE
        }
        mVideoViewPlaying = videoView
        videoView.visibility = View.VISIBLE
        loading.visibility = View.VISIBLE
        videoView.setVideoURI(Uri.parse(item.preview))
        videoView.setOnPreparedListener { mp: MediaPlayer ->
            mp.start()
            if (mp.isPlaying) {
                loading.visibility = View.GONE
            } else {
                loading.visibility = View.VISIBLE
            }
        }
        videoView.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            loading.visibility = View.GONE
            mVideoViewPlaying!!.visibility = View.GONE
            videoView.visibility = View.GONE
            Toast.makeText(mContext, "video error", Toast.LENGTH_SHORT).show()
            true
        }
        videoView.setOnCompletionListener { mp: MediaPlayer? ->
            mVideoViewPlaying!!.stopPlayback()
            mVideoViewPlaying!!.visibility = View.GONE
            mVideoViewPlaying = null
            loading.visibility = View.GONE
        }
        videoView.start()
    }
}