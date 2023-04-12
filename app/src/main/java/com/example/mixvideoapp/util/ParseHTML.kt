package com.example.mixvideoapp.util

import android.util.Log
import com.example.mixvideoapp.model.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

object ParseHTML {

    fun parsePhotos(type: String, html: String): List<MixPhoto> {
        var list = ArrayList<MixPhoto>()
        val document = Jsoup.parse(html)
        when (type) {
            Consts.TYPE_LES8 -> {
                // get les photo
                val els = document.getElementsByClass("images").select("a")
                for (it in els) {
                    var small = it.select("img").attr("src")
                    if (small.contains("image/gif;base64")) {
                        small = it.select("img").attr("data-original")
                    }
                    val big = it.attr("href")
                    val mixPhoto = MixPhoto(small, big, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
            "getScreenshots" -> {
                // getScreenshots
                val els = document.getElementById("tab_screenshots")
                    .select("div[class=block-screenshots]").select("a")
                for (it in els) {
                    var small = it.select("img").attr("src")
                    if (small.contains("image/gif;base64")) {
                        small = it.select("img").attr("data-original")
                    }
                    val big = it.attr("href")
                    val mixPhoto = MixPhoto(small, big, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
            Consts.TYPE_VIET -> {
                // get viet photo
                val els = document.select("div[class=ci]").select("div[class=separator]")
                for (it in els) {
                    val small = it.select("img").attr("data-src")
                    val mixPhoto = MixPhoto(small, small, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
            Consts.TYPE_EN -> {
                // get en photo
                val elements = document.getElementsByClass("gallery-holder js-album")
                    .select("a[class=item]")
                for (it in elements) {
                    //https://albums193.zbporn.tv/main/1250x250/79000/79243/1884758.jpg
                    //https://albums193.zbporn.tv/main/9998x9998/79000/79243/1884758.jpg
                    var small: String? = ""
                    small = if (it.select("img").attr("data-src").isNullOrEmpty()) {
                        it.select("img").attr("src")
                    } else {
//                        it.select("img").attr("data-src")
                        it.select("img").attr("src")
                    }
                    val big = it.attr("href")
                    val mixPhoto = MixPhoto(small, big, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
            Consts.TYPE_TRAN -> {
                // get trans photo
                /*
                     * thumb - https://cdnimages.shemaletubevideos.com/images/galleries/0593/17965/9cfac42258f099fc2551bed422833470-t.jpg
                     * full  - https://cdnimages.shemaletubevideos.com/images/galleries/0593/17965/9cfac42258f099fc2551bed422833470.jpg
                     * */
                val els = document.getElementsByClass("thumbs-5").select("div[class=th4]")
                for (it in els) {
                    val small = it.select("img").attr("src")
                    val big = it.select("a").attr("href")
                    val mixPhoto = MixPhoto(small, big, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
            else -> {
                // get ani photo
                //https://www.zooskoolvideos.com/media/photos/thumbs/2081.jpg
                //https://www.zooskoolvideos.com/media/photos/2081.jpg
                var photos = document.getElementsByClass("tab-content")
                    .select("ul[class=photos]").select("li[class=photo]")
                photos.forEach {
                    var small = "https://www.zookings.com" + it.select("img").attr("src")
                    var big = small.replace("thumbs/", "")
                    val mixPhoto = MixPhoto(small, big, type)
                    list.add(mixPhoto)
                    Log.e("mixPhoto", "${mixPhoto}")
                }
            }
        }
        return list
    }

    fun parseAudios(html: String): List<MixAudio> {
        var list = ArrayList<MixAudio>()
        val document = Jsoup.parse(html)
        val elements = document.select("div[class=video-section]")
            .select("div[class=item col-xl-4 col-lg-4 col-md-4 col-sm-6 col-6]")
        elements.forEach {
            val href = it.select("div[class=item-img]").select("a").attr("href")
            val thumb = it.select("div[class=item-img]").select("img").attr("src")
            val title = it.select("div[class=post-header]").select("h3[class=post-title]").text()
            val date = it.select("div[class=post-header]").select("span[class=date]").text()
            val views = it.select("div[class=post-header]").select("span[class=views]").text()
            val mixAudio = MixAudio(title, thumb, href, date, views)
            list.add(mixAudio)
            Log.e("parseAudios", "parseAudios: ${mixAudio}")
        }
        return list
    }

    fun parseAlbums(type: String, html: String): List<MixAlbum> {
        var list = ArrayList<MixAlbum>()
        val document = Jsoup.parse(html)
        when (type) {
            Consts.TYPE_LES8 -> {
                val els = document.getElementsByClass("list-albums")
                    .select("div[class=item]")
                els.forEach {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
                    var thumb = it.select("img").attr("src")
                    if (thumb.contains("image/gif;base64")) {
                        thumb = it.select("img").attr("data-original")
                    }
                    val count = it.select("div[class=photos]").text()
                    val mixAlbum = MixAlbum(title, thumb, href, count, type)
                    list.add(mixAlbum)
                    Log.e("parseAlbum", "parseAlbum: $mixAlbum")
                }
            }
            Consts.TYPE_VIET -> {
                val els = document.getElementsByClass("tidymag-posts-container")
                    .select("div[class=tidymag-cgrid-post]")
                els.forEach {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
                        .split(" - Maulon").toTypedArray()[0]
                    val thumb = it.select("img").attr("src")
                    val count = it.select("div[class=iconimage]").text()
                    val mixAlbum = MixAlbum(title, thumb, href, count, type)
                    list.add(mixAlbum)
                    Log.e("parseAlbum", "parseAlbum: $mixAlbum")
                }
            }
            Consts.TYPE_EN -> {
                val elements: Elements = document.select("div[class=thumbs thumbs-albums]")
                    .select("div[class=th]")
                for (it in elements) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
//                    val thumb = "https:" + it.select("img").attr("data-src")
                    val thumb = "https:" + it.select("img").attr("src")
                    val count = it.select("span[class=th-duration]").text().substringBefore(" ")
                    if (thumb.contains(".jpg") && !count.isEmpty() && !title.isEmpty() && !href.isEmpty()) {
                        val mixAlbum = MixAlbum(title, thumb, href, count, type)
                        list.add(mixAlbum)
                        Log.e("parseAlbum", "parseAlbum: $mixAlbum")
                    }
                }
            }
            Consts.TYPE_TRAN -> {
                val els = document.getElementsByClass("thumbs-wrap").select("div[class=thfea]")
                for (it in els) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").text()
                    val thumb = it.select("img").attr("src")
                    val count = it.select("p[class=lenght]").text().split(" ").toTypedArray()[0]
                    val mixAlbum = MixAlbum(title, thumb, href, count, type)
                    list.add(mixAlbum)
                    Log.e("parseAlbum", "parseAlbum: $mixAlbum")
                }
            }
            else -> {
                val els = document.getElementsByClass("panel-body")
                    .select("li[class=album]")
                els.forEach {
                    if (it.select("div[class=private-overlay]").isNullOrEmpty()) {
                        val href = "https://www.zookings.com" + it.select("a")
                            .attr("href")
                        val title = it.select("span[class=title]").select("a").text()
                        val thumb = "https://www.zookings.com" + it.select("img")
                            .attr("src")
                        val count = it.select("span[class=duration]").first().text()
                        val mixAlbum = MixAlbum(title, thumb, href, count, type)
                        list.add(mixAlbum)
                        Log.e("parseAlbum", "parseAlbum: $mixAlbum")
                    }
                }
            }
        }
        return list
    }

    fun parseCategories(type: String, html: String): List<MixCategory> {
        var list = ArrayList<MixCategory>()
        val document = Jsoup.parse(html)
        when (type) {
            Consts.TYPE_LES8 -> {
                val elements: Elements = document.getElementsByClass("list-categories")
                    .select("a[class=item]")
                for (it in elements) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
                    val max = it.select("div[class=videos]").text()
                        .substringBefore(" ").toInt()
                    var thumb = it.select("img").attr("src")
                    if (thumb.contains("image/gif;base64")) {
                        thumb = it.select("img").attr("data-original")
                    }
                    val mixCategory = MixCategory(title, thumb, href, type, max)
                    list.add(mixCategory)
                    Log.e("parseCategories", "${it.elementSiblingIndex()}: ${mixCategory}")
                }
            }
            Consts.TYPE_CUTO -> {
                val elements: Elements = document.getElementsByClass("list-videos ct")
                    .select("a[class=item category_item]")
                for (it in elements) {
                    val href = it.attr("href")
                    val title = it.attr("title").uppercase(Locale.ROOT)
                    val max = it.select("span[class=duration]").text()
                        .substringBefore(" ").toInt()
                    val thumb = it.select("img").attr("data-original")
                    val mixCategory = MixCategory(title, thumb, href, type, max)
                    list.add(mixCategory)
                    Log.e("parseCategories", "parseCategories: ${mixCategory}")
                }
            }
            "en_cate" -> {
                val elements: Elements = document.select("div[class=thumbs thumbs-categories]")
                    .select("div[class=th]")
                for (it in elements) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
                    var thumb = ""
                    if (it.elementSiblingIndex() < 4) {
                        thumb = it.select("img").attr("src")
                    } else {
                        thumb = it.select("img").attr("data-src")
                    }
                    val mixCategory = MixCategory(title, thumb, href, type, 0)
                    list.add(mixCategory)
                    Log.e("parseCategories", "parseCategories: ${mixCategory}")
                }
            }
            "ani_cate" -> {
                val elements = document.getElementsByClass("thumbs mgb").select("div[class=th]")
                elements.forEach {
                    val title = it.select("div[class=th-category]").text()
                    val thumb = "https://zoozoosexporn.com" + it.select("img").attr("src")
                    val href = "https://zoozoosexporn.com" + it.select("a").attr("href")
                        .substringBeforeLast("/").substringBeforeLast("/")
                    val mixCategory = MixCategory(title, thumb, href, type, 0)
                    list.add(mixCategory)
                    Log.e("parseCategories", "parseCategories: ${mixCategory}")
                }
            }
            "tran_cate" -> {
                list = Consts.LIST_CATE_TRAN
            }
            else -> {
                list = Consts.LIST_CATE_VN
            }
        }
        return list
    }

    fun parseAudioSource(html: String): String {
        var url = ""
        val document = Jsoup.parse(html)
        val source = document.select("div[class=player player-large player-wrap]")
        url = source.toString().substringAfter("xsources_mp3\">").substringBefore("</li>")
        Log.e("parseAudioSource", "${url}")
        return url
    }

    fun parseVideoSource(type: String, html: String): String {
        var url = ""
        val document = Jsoup.parse(html)
        when (type) {
            Consts.TYPE_VIET -> {
                // get viet video
                val html = document.toString()
                val data = html.split("html5player.setVideoUrlHigh")
                    .toTypedArray()[1].split(";").toTypedArray()[0]
                url = data.replace("('", "").replace("')", "")
            }
            Consts.TYPE_EN -> {
                // get zb video
                val source = document.getElementsByClass("player slot-1").toString()
                url = source.split("video_url: '").toTypedArray()[1].split("',").toTypedArray()[0]
            }
            Consts.TYPE_TRAN -> {
                // get trans video
                val dloaddivcol = document.getElementsByClass("xp_sec_box").select("ul")
                url = dloaddivcol.eq(1).select("a").last().attr("href")
            }
            else -> {
                // get ani video
                url = document.getElementsByClass("video-player")
                    .select("source").attr("src")
            }
        }
        Log.e("parseVideoSource", "${url}")
        return url
    }

    fun parseVideos(type: String, html: String): List<MixVideo> {
        val list = ArrayList<MixVideo>()
        val document = Jsoup.parse(html)
        when (type) {
            Consts.TYPE_LES8 -> {
                val elLes8 = document.getElementsByClass("list-videos")
                    .select("div[class=item]")
                for (it in elLes8) {
                    val link = it.select("a").attr("href")
                    val href = Consts.URL_LES8 + "/embed/" + it.select("a").attr("href")
                        .substringAfterLast("videos/").substringBefore("/")
                    val title = it.select("a").attr("title")
                    var thumb = it.select("img").attr("src")
                    if (thumb.contains("data:image/gif;base64")) {
                        thumb = it.select("img").attr("data-original")
                    }
                    val duration = it.select("div[class=duration]").text()
                    val time = it.select("div[class=added]").text()
                    val trailer = it.select("img").attr("data-preview")
                    val mixVideo = MixVideo(title, thumb, duration, href, trailer, type, link, time)
                    list.add(mixVideo)
                    Log.e("mixVideo", "${mixVideo}")
                }
            }
            Consts.TYPE_CUTO -> {
                val elCuto = document.getElementsByClass("list-videos")
                    .select("p[class=item]")
                for (it in elCuto) {
                    val href = Consts.URL_CUTO + "/embed/" + it.select("a")
                        .attr("href").substringAfterLast("/")
                    val title = it.select("a").text()
                    var thumb = it.select("img").attr("src")
                    if (thumb.contains("/preload.svg")) {
                        thumb = it.select("img").attr("data-original")
                    }
                    //<span class="duration">11:00<em class="views">1091</em></span>
                    val duration = it.select("span[class=duration]").toString()
                        .substringAfter("duration\">").substringBefore("<em")
                    val trailer = it.select("img").attr("data-preview")
                    val mixVideo = MixVideo(title, thumb, duration, href, trailer, type)
                    list.add(mixVideo)
                    Log.e("mixVideo", "${mixVideo}")
                }
            }
            Consts.TYPE_VIET -> {
                val elViet: Elements = document.getElementsByClass("mozaique cust-nb-cols")
                    .select("div[class=thumb-block  ]")
                for (it in elViet) {
                    val title = it.getElementsByClass("thumb-under").select("a").attr("title")
                    val thumb = it.getElementsByClass("thumb-inside").select("img").attr("data-src")
                    val idData = it.attr("data-id")
                    val href = "https://xnxx98.net/embedframe/$idData"
                    val duration = getDuration(
                        it.getElementsByClass("bg").select("span[class=duration]").text()
                    )
                    val trailer = getTrailer(thumb)
                    val mixVideo = MixVideo(title, thumb, duration, href, trailer, type)
                    list.add(mixVideo)
                    Log.e("mixVideo", "${mixVideo}")
                }
            }
            Consts.TYPE_EN -> {
                val elements: Elements = document.select("div[class=th]")
//        Elements elements = document.select("div[class=thumbs]").select("div[class=th]");
                //        Elements elements = document.select("div[class=thumbs]").select("div[class=th]");
                for (it in elements) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").attr("title")
                    var thumb = it.select("img").attr("src")
                    if (thumb.contains("data:image/gif;base64")) {
                        thumb = it.select("img").attr("data-src")
                    }
                    val trailer = it.select("a[class=th-image-link]").attr("data-preview")
                    val duration = it.select("div[class=th-image]").select("span").text()
                    if (!duration.isEmpty() && !thumb.isEmpty()) {
                        val mixVideo = MixVideo(title, thumb, duration, href, trailer, type)
                        list.add(mixVideo)
                        Log.e("mixVideo", "${mixVideo}")
                    }
                }
            }
            Consts.TYPE_TRAN -> {
                val elTrans: Elements = document.getElementsByClass("thumbs-wrap")
                    .select("div[class=th]")
                for (it in elTrans) {
                    val href = it.select("a").attr("href")
                    val title = it.select("a").text()
                    val thumb = it.select("img").attr("src")
                    val duration = it.select("p[class=lenght]").text()
                    val trailer = it.select("video").attr("src")
                    val mixVideo = MixVideo(title, thumb, duration, href, trailer, type)
                    list.add(mixVideo)
                    Log.e("mixVideo", "${mixVideo}")
                }
            }
            else -> {
                val elements = document.getElementsByClass("thumbs mgb").select("div[class=th]")
                elements.forEach {
                    val title = it.select("div[class=th-title]").text()
                    val thumb = "https://zoozoosexporn.com" + it.select("img")
                        .attr("data-original")
                    val href = "https://zoozoosexporn.com" + it.select("a")
                        .attr("href").substringAfter("=")
                    val duration = it.select("span[class=th-duration]").text()
                    val trailer = it.select("img").attr("data-preview")
                    if (!duration.isNullOrEmpty() && !trailer.isNullOrEmpty()) {
                        val mixVideo = MixVideo(title, thumb, duration, href, trailer, type)
                        list.add(mixVideo)
                        Log.e("mixVideo", "${mixVideo}")
                    }
                }
            }
        }
        return list
    }

    private fun getDuration(time: String): String {
        // 11 min - 48 sec -  1 h 37 min
        if (time != null && !time.isEmpty()) {
            if (time.contains("sec")) {
                return time.replace("sec".toRegex(), "giây")
            }
            if (time.contains("min")) {
                return time.replace("min".toRegex(), "phút")
            }
            if (time.contains("h") && time.contains("min")) {
                return time.replace("h".toRegex(), "giờ").replace("min".toRegex(), "phút")
            }
            if (time.contains("trước")) {
                return time.replace("trước".toRegex(), "")
            }
        }
        return "00:00"
    }

    private fun getTrailer(thumb: String): String {
        var trailer = ""
        if (thumb != null && !thumb.isEmpty()) {
            val b = thumb.split("thumbs").toTypedArray()[1]
            val c = b.substring(0, b.lastIndexOf("/")).substring(6)
            val head = thumb.split("thumbs").toTypedArray()[0]
            trailer = head + "videopreview/" + c + "_169.mp4"
            if (trailer.contains("-1_169")) {
                trailer = trailer.replace("-1".toRegex(), "")
            }
        } else {
            trailer = ""
        }
        return trailer
    }

    private fun fakeCount(isViews: Boolean): String? {
        var min = 0
        var max = 0
        if (isViews) {
            max = 10000000
            min = 1000
        } else {
            max = 100
            min = 50
        }
        val random = Random()
        val randomNumber = random.nextInt(max + 1 - min) + min
        //        int randomNumber = random.nextInt(3000000 + 1 - 100) + 100;
        val decimalFormat = DecimalFormat("#,###")
        return decimalFormat.format(randomNumber.toLong())
    }
}