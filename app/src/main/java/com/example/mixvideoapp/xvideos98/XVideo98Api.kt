package com.example.mixvideoapp.xvideos98

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface XVideo98Api {

    //https://xvideos98.xxx/new/1?sort=uploaddate
    @GET("/new/{page}")
    fun getVideos(
        @Path("page") page: Int,
        @Query("sort") sort: String?
    ): Observable<String>

    //https://xvideos98.xxx/?k=big+boss&sort=uploaddate&p=1
    @GET("/")
    fun searchVideos(
        @Query("k") query: String,
        @Query("sort") sort: String?,
        @Query("p") page: Int
    ): Observable<String>

    @GET
    fun getVideoSource(
        @Url herf: String
    ): Observable<String>
}
