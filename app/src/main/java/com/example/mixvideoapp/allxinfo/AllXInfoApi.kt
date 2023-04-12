package com.example.mixvideoapp.allxinfo

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface AllXInfoApi {

    @GET
    fun getVideos(@Url url: String): Observable<String>
}