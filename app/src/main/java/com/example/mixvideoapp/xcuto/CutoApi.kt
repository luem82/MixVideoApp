package com.example.mixvideoapp.xcuto

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface CutoApi {

    //https://sexcuto.me/latest-updates/2
    //https://sexcuto.me/top-rated/2
    //https://sexcuto.me/most-popular/2
    @GET("/{sort}/{page}")
    fun getVideos(
        @Path("sort") sort: String,
        @Path("page") page: Int
    ): Observable<String>

    //https://sexcuto.me/search/n%C6%B0%E1%BB%9Bc?mode=async&function=get_block&block_id=list_videos_videos_list_search_result&q=n%C6%B0%E1%BB%9Bc&category_ids=&sort_by=post_date&from_videos=1&from_albums=1
    @GET
    fun searchVVideos(@Url url: String): Observable<String>

    //https://sexcuto.me/categories
    @GET("/categories/{page}")
    fun getAllCategories(
        @Path("page") page: Int
    ): Observable<String>

    //https://sexcuto.me/categories/masturbation/2
    @GET
    fun getVideosByCategory(@Url url: String): Observable<String>

    @GET
    fun getVideoDetail(@Url url: String): Observable<String>

}