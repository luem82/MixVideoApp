package com.example.mixvideoapp.xvideos98

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

object ParseXvideos98 {

    const val TAG = "ParseXvideos98"

    //  https://xnxx98.net/embedframe/74143315
    fun parseSource(html: String): String {
        val document = Jsoup.parse(html)
        val html = document.toString()
        var url = ""
        try {
            if (html.contains("setVideoHLS")) {
                val data = html.split("html5player.setVideoHLS").toTypedArray()[1]
                    .split(";").toTypedArray()[0]
                url = data.replace("('", "").replace("')", "")
            } else if (html.contains("setVideoUrlHigh")) {
                val data = html.split("html5player.setVideoUrlHigh").toTypedArray()[1]
                    .split(";").toTypedArray()[0]
                url = data.replace("('", "").replace("')", "")
            } else {
                val data = html.split("html5player.setVideoUrlLow").toTypedArray()[1]
                    .split(";").toTypedArray()[0]
                url = data.replace("('", "").replace("')", "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        Log.e("parseVideoSource", "${url}")
        Log.e("parseVideoSource", "${document.html()}")
        return url
    }

    fun parseVideos(html: String): List<Xvideos98> {
        val list = ArrayList<Xvideos98>()
        val document = Jsoup.parse(html)
        val elViet: Elements = document.getElementsByClass("mozaique cust-nb-cols")
            .select("div[class=thumb-block  ]")
        for (it in elViet) {
            val link = XConsts.BASE_URL + it.getElementsByClass("thumb-under")
                .select("a").attr("href")
            val title = it.getElementsByClass("thumb-under").select("a").attr("title")
            val thumb = it.getElementsByClass("thumb-inside").select("img").attr("data-src")
            val idData = it.attr("data-id")
            val href = "https://xnxx98.net/embedframe/$idData"
//            val href = "https://xvideos98.xxx/embedframe/$idData"
            val duration = getDuration(
                it.getElementsByClass("bg").select("span[class=duration]").text()
            )
            val preview = getTrailer(thumb)
            val views = fakeCount(true) + " lượt xem"
            val rates = fakeCount(false) + "%"
            val xvideos98 = Xvideos98(title, thumb, duration, href, link, preview, views, rates)
            list.add(xvideos98)
            Log.e("parseXvideos98:", "${it.elementSiblingIndex()} : ${xvideos98}")
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