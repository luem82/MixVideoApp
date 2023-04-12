package com.example.mixvideoapp.allxinfo

import android.util.Log
import com.example.mixvideoapp.allxinfo.AllXInfoActivity.Companion.ALLX_URL
import org.jsoup.Jsoup

object ParseAllXIInfo {

    private const val TAG = "ParseAllXIInfo"

    fun parseCategories(html: String): List<AllXCategory> {
        val list = ArrayList<AllXCategory>()
        val document = Jsoup.parse(html)
        val elVideos = document.getElementsByClass("list-categories")
            .select("a[class=item]")
        elVideos.forEach {
            val title = it.attr("title")
            val href = it.attr("href")
            var thumb = it.select("img").attr("src")
            if (thumb.contains("data:image/gif;base64")) {
                thumb = it.select("img").attr("data-original")
            }
            val count = it.select("div[class=videos]").text()
            val rates = it.select("div[class=rating positive]").text()
            val views = it.select("div[class=views]").text()
            if (!thumb.isNullOrEmpty()) {
                val allXCategory = AllXCategory(title, count, rates, thumb, href)
                list.add(allXCategory)
                Log.e(TAG, "parseCategories: ${it.elementSiblingIndex()} - ${allXCategory}")
            }
        }
        return list
    }

    fun parseVideos(html: String): List<AllXVideo> {
        val list = ArrayList<AllXVideo>()
        val document = Jsoup.parse(html)
        val elVideos = document.getElementsByClass("list-videos")
            .select("div[class=item]")
        elVideos.forEach {
            val title = it.select("a").attr("title")
            val href = it.select("a").attr("href")
            var thumb = it.select("a").select("img").attr("src")
            if (thumb.contains("data:image/gif;base64")) {
                thumb = it.select("img").attr("data-original")
            }
            val duration = it.select("div[class=duration]").text()
            val rates = it.select("div[class=rating positive]").text()
            val views = it.select("div[class=views]").text()
            val date = it.select("div[class=added]").text()
            val trailer = "trailer"
            //https://allx.info/embed/5321
            val embed = ALLX_URL + "/embed/" + href.substringBeforeLast("/")
                .substringBeforeLast("/").substringAfterLast("/")
            val allXVideo = AllXVideo(
                title, thumb, views, rates, duration, date, href, embed, trailer
            )
            list.add(allXVideo)
            Log.e(TAG, "parseVideo: ${it.elementSiblingIndex()} - ${allXVideo}")
        }
        return list
    }

    fun parseDetailVideo(html: String): InfoDetail {
        val document = Jsoup.parse(html)
        val videoTitle = document.select("div[class=headline]").select("h1").text()
        val description = document.getElementsByClass("info")
            .select("div[class=item]").next().text().substringBefore("Thể loại:")
        val videoSource = document.getElementsByClass("info")
            .select("div[class=item]").last().select("a").attr("href")
        val listRelated = parseVideos(html)
        val infoDetail = InfoDetail(videoTitle, description, videoSource, listRelated)
        Log.e(TAG, "parseDetailVideo: ${description}")
        Log.e(TAG, "parseDetailVideo: ${videoSource}")
//        Log.e(TAG, "parseDetailVideo: ${infoDetail}")
        return infoDetail
    }
}
















