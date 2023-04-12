package com.example.mixvideoapp.pornogids

import android.util.Log
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList

object ParseGids {

    private const val TAG = "ParseGids"

    fun parseVideos(html: String): List<GidsVideo> {
        val list = ArrayList<GidsVideo>()
        val document = Jsoup.parse(html)
        val elements = document.getElementsByClass("item video-item  ")
        Log.e("source", "${elements.size}")
        elements.forEach {
            val rates = it.select("div[class=rating positive]").text()
            if (!rates.isNullOrEmpty()) {
                val href = it.select("a").attr("href")
                val title = it.select("div[class=title_wrap]").text()
                val thumb = it.select("img").attr("data-src")
                val duration = it.select("div[class=duration]").text()
                val views = it.select("div[class=views]").text().replace(" ", ",")

                val gidsVideo = GidsVideo(title, thumb, href, views, rates, duration)
                list.add(gidsVideo)
                Log.e(TAG, "${it.elementSiblingIndex()} - ${gidsVideo}")
            }
        }
        return list
    }

    fun parseCategories(html: String): List<GidsCategory> {
        val list = ArrayList<GidsCategory>()
        val document = Jsoup.parse(html)
        val elements = document.getElementById("list_categories_categories_list_items")
            .select("a[class=item]")
        Log.e(TAG, "parseCategories list size: ${elements.size}")
        elements.forEach {
            val href = it.select("a").attr("href")
            val key = href.substringBeforeLast("/").substringAfterLast("/")
            val title = it.select("a").attr("title")
            val thumb = it.select("img").attr("data-src")
            val count = it.select("div[class=videos]").text()
            val rates = it.select("div[class=rating positive]").text()
            val gidsCategory = GidsCategory(title, thumb, href, key, count, rates)
            list.add(gidsCategory)
            Log.e(TAG, "${it.elementSiblingIndex()} - ${gidsCategory}")
        }
        return list
    }

    fun parseDetailVideo(html: String): DetailGidsVideo {
        val document = Jsoup.parse(html)
        val listRelated = parseVideos(html)
        var date = "mới nhất"
        val source = document.select("video[id=player]").select("source").attr("src")
        var description = "Video không có mô tả"
        val elInfos = document.select("div[class=block-details]")
            .select("div[class=info]").select("div[class=item]")
        elInfos.forEach {
            if (it.text().contains("Description")) {
                val des = it.getElementsByTag("em").text()
                description = des
                Log.e("result", "Description : ${des}")
                //Submitted:
            } else if (it.text().contains("Submitted")) {
                val time = it.getElementsByTag("span").text()
                //date=Duration: 26:10 Views: 3 Submitted: 1 hour ago
                date = "Phát hành:" + time.substringAfter("Submitted:")
                    .replace("ago", "trước")
                    .replace("minutes", "phút")
                    .replace("hours", "giờ")
                    .replace("days", "ngày")
                    .replace("months", "tháng")
                    .replace("year", "năm")
                    .replace("minute", "phút")
                    .replace("hour", "giờ")
                    .replace("day", "ngày")
                    .replace("month", "tháng")
                    .replace("year", "năm")
                Log.e("result", "Description : ${time}")
            } else if (it.text().contains("Categories")) {
//                it.select("a").forEach {
//                    val title = it.text()
//                    val href = it.attr("href")
//                    listCategory.add(GidTag(title, href))
//                    Log.e("result", "Categories : ${title} - ${href}")
//                }
            } else if (it.text().contains("Tags")) {
                it.select("a").forEach {
//                    val title = it.text()
//                    val href = it.attr("href")
//                    listTag.add(GidTag(title, href))
//                    Log.e("result", "Tags : ${title} - ${href}")
                }
            }
        }
        val detailGidsVideo = DetailGidsVideo(source, date, description, listRelated)
        return detailGidsVideo
    }
}





