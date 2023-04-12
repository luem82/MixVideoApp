package com.example.mixvideoapp.util

object Helpers {

    fun getPlayerPercent(current: Long, total: Long): Int {
        var percent = 0
        if (current > total || current == total) {
            percent = 100
        } else {
            percent = (current * 100 / total).toInt()
        }
        return if (percent < 1) {
            3
        } else percent
    }

    fun createUrlGetVideos(mType: String, mSort: String, mPage: Int, mQuery: String): String {
        var mUrl = ""
        when (mType) {
            Consts.TYPE_VIET -> {
                mUrl = "https://xvideos98.xxx/new/${mPage}?sort=${mSort}"
            }
            Consts.TYPE_EN -> {
                mUrl = "https://zbporn.tv/${mSort}/${mPage}/"
            }
            Consts.TYPE_TRAN -> {
                mUrl = "https://www.shemaletubevideos.com/videos/${mSort}-${mPage}.html"
            }
            Consts.TYPE_ANI -> {
                mUrl = "https://zoozoosexporn.com/en/all/${mSort}/${mPage}/"
            }
            else -> {
                when (mSort) {
                    Consts.TYPE_VIET -> {
                        //https://xvideos98.xxx/?k=stepmom%20and%20son
                        mUrl = "https://xvideos98.xxx/?k=${mQuery}&p=${mPage}"
                    }
                    Consts.TYPE_EN -> {
                        //https://zbporn.tv/search/mom+and+son/2/
                        mUrl = "https://zbporn.tv/search/${mQuery}/${mPage}/"
                    }
                    Consts.TYPE_TRAN -> {
                        //https://www.shemaletubevideos.com/search/video/mom+and+son/2/
                        mUrl = "https://www.shemaletubevideos.com/search/video/${mQuery}/${mPage}/"
                    }
                    else -> {
                        //https://zoozoosexporn.com/en/search/teen+dog/best/1/
                        mUrl = "https://zoozoosexporn.com/en/search/${mQuery}/best/${mPage}/"
                    }
                }
            }
        }
        return mUrl
    }

    fun createUrlGetAlbums(mType: String, mPage: Int): String {
        var mUrl = ""
        when (mType) {
            Consts.TYPE_VIET -> {
                //https://5sex.maulon.pro/page/3/
                mUrl = "https://5sex.maulon.pro/page/${mPage}/"
            }
            Consts.TYPE_EN -> {
                //https://zbporn.tv/albums/2/
                mUrl = "https://zbporn.tv/albums/${mPage}/"
            }
            Consts.TYPE_TRAN -> {
                //https://www.shemaletubevideos.com/galleries/all-recent-2.html
                mUrl = "https://www.shemaletubevideos.com/galleries/all-popular-${mPage}.html"
            }
            else -> {
                //https://www.zookings.com/photos/viewed/2/
                if (mPage == 1) {
                    mUrl = "https://www.zookings.com/photos/"
                } else {
                    mUrl = "https://www.zookings.com/photos/${mPage}/"
                }
            }
        }
        return mUrl
    }
}