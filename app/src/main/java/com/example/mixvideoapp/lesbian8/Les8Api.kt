package com.example.mixvideoapp.lesbian8

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface Les8Api {

    //https://www.lesbian8.com/latest-updates/2/
    //https://www.lesbian8.com/most-popular/2/
    @GET("/{sort}/{page}/")
    fun getVideos(
        @Path("sort") sort: String,
        @Path("page") page: Int
    ): Observable<String>

    @GET
    fun getScreenshots(@Url link: String): Observable<String>

    //https://www.lesbian8.com/categories/
    @GET("/categories/")
    fun getAllCategories(): Observable<String>

    //https://www.lesbian8.com/categories/african/2/
    @GET
    fun getVideosByCategory(@Url link: String): Observable<String>

    //https://www.lesbian8.com/albums/3/
    //https://www.lesbian8.com/albums/2/?mode=async&function=get_block&block_id=list_albums_common_albums_list&sort_by=post_date&_=1673609901225
    //sort_by:post_date
    //sort_by:rating
    //sort_by:most_favourited
    //sort_by:most_commented
    @GET("/albums/{page}/")
    fun getAllAlbums(
        @Path("page") page: Int
    ): Observable<String>

    @GET
    fun getPhotos(@Url href: String): Observable<String>

}