package com.example.mixvideoapp.hdtube

import android.util.Log
import org.jsoup.Jsoup

object ParseHDTube {

    private const val TAG = "ParseHDTube"

    fun parseHomePage(html: String) {
        val document = Jsoup.parse(html)

        // new videos list
        val newEl = document.getElementById("list_videos_videos_watched_right_now_items")
            .select("div[class=item cards__item]")
        val newList = arrayListOf<HDTubeVideo>()
        newEl.forEach {
            val title = it.getElementsByClass("card__content").select("a")
                .attr("title")
            val href = it.getElementsByClass("card__content").select("a")
                .attr("href")
            val trailer = it.getElementsByClass("card__content").select("a")
                .attr("data-preview")
            val thumb = it.getElementsByClass("card__content").select("a")
                .select("img").attr("src")
            val duration = it.getElementsByClass("card__content")
                .select("span[class=card__flag]").text()
            val hdTubeVideo = HDTubeVideo(title, thumb, duration, trailer, href)
            newList.add(hdTubeVideo)
            Log.e(TAG, "New videos: ${it.elementSiblingIndex()} - ${hdTubeVideo}")
        }

        // more videos list
        val moreEl = document.getElementById("list_videos_most_recent_videos_items")
            .select("div[class=item cards__item]")
        val moreList = arrayListOf<HDTubeVideo>()
        moreEl.forEach {
            val title = it.getElementsByClass("card__content").select("a")
                .attr("title")
            val href = it.getElementsByClass("card__content").select("a")
                .attr("href")
            val trailer = it.getElementsByClass("card__content").select("a")
                .attr("data-preview")
            val thumb = it.getElementsByClass("card__content").select("a")
                .select("img").attr("src")
            val duration = it.getElementsByClass("card__content")
                .select("span[class=card__flag]").text()
            val hdTubeVideo = HDTubeVideo(title, thumb, duration, trailer, href)
            moreList.add(hdTubeVideo)
            Log.e(TAG, "More videos: ${it.elementSiblingIndex()} - ${hdTubeVideo}")
        }

        // first categories list
        val cateEl = document.select("div[class=cat-drop]")
            .select("a[class=cat-drop__item]")
        val categoriesList = arrayListOf<HDTubeCategory>()
        cateEl.forEach {
            val title = it.select("span[class=cat-drop__title capitalize]").text()
            val thumb = it.select("picture").select("img").attr("src")
            val href = it.attr("href")
            val hdTubeCategory = HDTubeCategory(title, thumb, "", "", href)
            categoriesList.add(hdTubeCategory)
            Log.e(TAG, "Categories: ${hdTubeCategory}")
        }

        Log.e(TAG, "New videos count: ${newList.size}")
        Log.e(TAG, "More videos count: ${moreList.size}")
        Log.e(TAG, "Categories count: ${categoriesList.size}")

    }

    fun parseVideos(html: String): List<HDTubeVideo> {
        val document = Jsoup.parse(html)
        val elements = document.select("div[class=item cards__item]")
        val list = arrayListOf<HDTubeVideo>()
        elements.forEach {
            val title = it.getElementsByClass("card__content").select("a")
                .attr("title")
            val href = it.getElementsByClass("card__content").select("a")
                .attr("href")
            val trailer = it.getElementsByClass("card__content").select("a")
                .attr("data-preview")
            val thumb = it.getElementsByClass("card__content").select("a")
                .select("img").attr("src")
            val duration = it.getElementsByClass("card__content")
                .select("span[class=card__flag]").text()
            val hdTubeVideo = HDTubeVideo(title, thumb, duration, trailer, href)
            list.add(hdTubeVideo)
            Log.e(TAG, "videos: ${it.elementSiblingIndex()} - ${hdTubeVideo}")
        }
        Log.e(TAG, "videos count: ${list.size}")
        return list
    }

    fun parseCategories(html: String): List<HDTubeCategory> {
        val list = arrayListOf<HDTubeCategory>()
        val document = Jsoup.parse(html)
        val cateEl = document.select("div[class=cards__list]")
            .select("div[class=card__content]").select("a")
        cateEl.forEach {
            val title = it.attr("title")
            val thumb = it.select("picture").select("img").attr("src")
            val href = it.attr("href")
            val hdTubeCategory = HDTubeCategory(title, thumb, "", "", href)
            list.add(hdTubeCategory)
            Log.e(TAG, "Categories: ${hdTubeCategory}")
        }

        return list
    }

    fun parseTags(html: String): List<String> {
        val list = arrayListOf<String>()
        val document = Jsoup.parse(html)
        val cateEl = document.select("div[class=tags__block]")
            .select("li[class=badge__item ]").select("a")
        cateEl.forEach {
            val title = it.attr("title")
            list.add(title)
            Log.e(TAG, "Tag: ${title}")
        }
        Log.e(TAG, "Tag count: ${list.size}")
        return list
    }

    fun parseInfodetail(html: String): HDTubeInfoDetail {
        var views = ""
        var date = ""
        var source = ""
        var description = ""
        var listTags = arrayListOf<String>()
        var listRelated = arrayListOf<HDTubeVideo>()
        var listModels = arrayListOf<HDTubeModel>()
        val document = Jsoup.parse(html)
        views = document.select("ul[class=watch__list]").select("li").eq(1).text()
            .substringAfter(" ").replace(" ", ",")
        date = document.select("ul[class=watch__list]").select("li").eq(2).text()
        description = document.select("div[class=description__row]").select("p").text()
        source = document.select("a[class=nav-link js-ga-download]").attr("href")
        val tagEl = document.select("li[class=badge__item]")
        tagEl.forEach {
            listTags.add(it.text())
        }

        val modelEl = document
            .select("ul[class=badge__list]")
            .select("li[class=badge__item]")
            .select("a[class=badge__link left-padding]")

        modelEl.forEach {
            val name = it.text()
            val href = it.attr("href")
            val thumb = it.select("img").attr("src")
            if (!thumb.contains("image/gif;base64")) {
                val hdTubeModel = HDTubeModel(name, thumb, href)
                listModels.add(hdTubeModel)
            }
        }

        val elements = document.select("div[class=item cards__item]")
        elements.forEach {
            val title = it.getElementsByClass("card__content").select("a")
                .attr("title")
            val href = it.getElementsByClass("card__content").select("a")
                .attr("href")
            val trailer = it.getElementsByClass("card__content").select("a")
                .attr("data-preview")
            val thumb = it.getElementsByClass("card__content").select("a")
                .select("img").attr("src")
            val duration = it.getElementsByClass("card__content")
                .select("span[class=card__flag]").text()
            val hdTubeVideo = HDTubeVideo(title, thumb, duration, trailer, href)
            listRelated.add(hdTubeVideo)
        }
        val hdTubeInfoDetail = HDTubeInfoDetail(
            views, date, description, source, listTags, listRelated, listModels
        )
        Log.e(TAG, "HDTubeInfoDetail: ${hdTubeInfoDetail}")
        return hdTubeInfoDetail
    }

    fun parseModels(html: String): List<HDTubeModel> {
        val document = Jsoup.parse(html)
        val elements = document.select("div[class=card__content card__content--landscape]")
        val list = arrayListOf<HDTubeModel>()
        elements.forEach {
            val name = it.select("a").attr("title")
            val href = it.select("a").attr("href")
            val thumb = it.select("a").select("img").attr("src")
            if (!thumb.contains("image/gif;base64")) {
                val hdTubeModel = HDTubeModel(name, thumb, href)
                list.add(hdTubeModel)
                Log.e(TAG, "model: ${it.elementSiblingIndex()} - ${hdTubeModel}")
            }
        }
        return list
    }

    fun parseInfoModel(html: String): HDTubeModelInfo {
        var birthday = "unknow"
        var age = "unknow"
        var place = "unknow"
        var rate = 1.5f
        val document = Jsoup.parse(html)
        rate = document.select("div[class=rating__list js-rating]")
            .attr("data-rating").toFloat()
        birthday = document.select("ul[class=info-list-models]")
            .select("li[class=info-list__item]").eq(1)
            .select("span[class=info-list__value]").text()
        age = document.select("ul[class=info-list-models]")
            .select("li[class=info-list__item]").eq(2)
            .select("span[class=info-list__value]").text()
        place = document.select("ul[class=info-list-models]")
            .select("li[class=info-list__item]").eq(3)
            .select("span[class=info-list__value]").text()
        val hdTubeModelInfo = HDTubeModelInfo(birthday, age, place, rate)
        Log.e(TAG, "hdTubeModelInfo: ${hdTubeModelInfo}")
        return hdTubeModelInfo
    }

}