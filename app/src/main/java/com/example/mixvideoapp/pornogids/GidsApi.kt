package com.example.mixvideoapp.pornogids

import io.reactivex.Observable
import io.reactivex.Observer
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface GidsApi {


    //https://pornogids.net/latest-updates/2/
    //https://pornogids.net/most-popular/2/
    @GET("{filter}/{page}/")
    fun getVideosFirst(
        @Path("filter") filter: String,
        @Path("page") page: Int,
    ): Observable<String>


    //https://pornogids.net/search/mom-and-son/3/
    @GET("search/{query}/{page}/")
    fun searchVideos(
        @Path("query") query: String,
        @Path("page") page: Int,
    ): Observable<String>

    //https://pornogids.net/categories/big-boobs/2/
    @GET("categories/{category}/{page}/")
    fun getVideosByCategory(
        @Path("category") category: String,
        @Path("page") page: Int,
    ): Observable<String>

    //https://pornogids.net/categories/
    @GET("categories/")
    fun getCategories(): Observable<String>


    @GET
    fun getDetailVideo(
        @Url href: String
    ): Observable<String>

}