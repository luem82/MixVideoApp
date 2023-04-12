package com.example.mixvideoapp.hdtube

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


interface HDTubeApi {

    /* Videos
    *https://www.hdtube.porn/ -> Home(new slide, popular, rated, longest, best of 2023, channels, categories, models)
    *https://www.hdtube.porn/videos/2/ -> most popular
    *https://www.hdtube.porn/videos/latest-updates/2/ -> latest updates
    *https://www.hdtube.porn/videos/top-rated/2/ -> top rated
    *https://www.hdtube.porn/videos/longest/2/ -> longest
    *https://www.hdtube.porn/best/year-2022/2/ -> best of year
    *https://www.hdtube.porn/search/mother+and+son/2/ -> search
    * */
    @GET("/{sub}/{filter}/{page}/")
    fun getHtmlVideos(
        @Path("sub") sub: String,
        @Path("filter") filter: String,
        @Path("page") page: Int,
    ): Observable<String>

    @GET(".")
    fun getHomPage(): Observable<String>

    @GET(".")
    fun getHtmlAllTags(): Observable<String>

    /* Categories
    * https://www.hdtube.porn/categories/ -> all categories
    * https://www.hdtube.porn/voyeur.porn/2/ -> categoty detail
    * https://www.hdtube.porn/asian.porn/2/
    * */
    @GET("/categories/")
    fun getHtmlAllCategories(): Observable<String>

    @GET
    fun getHtmlVideosByUrl(
        @Url url: String
    ): Observable<String>


    /* Channels
    * https://www.hdtube.porn/channels/2/ -> all channels
    * https://www.hdtube.porn/channels/sweet-sinner/2/ -> channel detail
    * */

    /* Models
    * https://www.hdtube.porn/pornstars/2/ -> all models
    * https://www.hdtube.porn/pornstars/2/?abc=D&gender=male -> filter males 'D'
    * https://www.hdtube.porn/pornstars/2/?abc=C&gender=female -> filter females 'C'
    * https://www.hdtube.porn/pornstars/2/?abc=A&gender=all -> filter all 'A'
    * https://www.hdtube.porn/pornstars/damon-dice/2/ -> model detail
    * */
    @GET("/pornstars/{page}/")
    fun getHtmlModels(
        @Path("page") page: Int,
        @Query("abc") letter: String,
        @Query("gender") gender: String
    ): Observable<String>

    @GET
    fun getHtmlInfoModel(
        @Url url: String
    ): Observable<String>


}