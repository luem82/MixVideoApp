package com.example.animaltube.network

import io.reactivex.Observable
import retrofit2.http.*

interface ApiMix {

    //https://zbporn.tv/search/mom+and+son/2/
    //https://xvideos98.xxx/?k=stepmom%20and%20son
    //https://www.shemaletubevideos.com/search/video/mom+and+son/2/
    //https://zoozoosexporn.com/en/search/teen+dog/best/1/


    //https://zoozoosexporn.com/en/Chick/best/2/
    //https://www.shemaletubevideos.com/videos/shemale/shemalewithshemale-recent.html
    //https://xvideos98.xxx/?k=B%C3%BA+Li%E1%BA%BFm&p=
    //https://zbporn.tv/categories/hd-videos/2/

    @GET
    fun getVideos(@Url url: String): Observable<String>

    @GET
    fun getVideoSource(@Url href: String): Observable<String>

    //https://zoozoosexporn.com/en/cats.php
    @GET
    fun getCategories(@Url url: String): Observable<String>

    //https://zoozoosexporn.com/en/Pony/best/1/
    @GET
    fun getDetailCategory(@Url href: String): Observable<String>

    //https://ngheaudiotruyen.info/page/2/
    //https://ngheaudiotruyen.info/nghe-audio-truyen-sex-mp3/page/2/?orderby=date
    //https://ngheaudiotruyen.info/nghe-audio-truyen-sex-mp3/page/2/?orderby=viewed
    //https://ngheaudiotruyen.info/nghe-audio-truyen-sex-mp3/page/2/?orderby=liked
    @GET("/nghe-audio-truyen-sex-mp3/page/{page}/")
    fun getAudios(
        @Path("page") page: Int
    ): Observable<String>

    @GET
    fun getAudioSource(@Url href: String): Observable<String>

    //https://www.zookings.com/photos/viewed/2/
    //https://www.zooskoolvideos.com/photos/recent/2/
    //https://zbporn.tv/albums/2/
    //https://www.shemaletubevideos.com/galleries/all-recent-2.html
    //https://5sex.maulon.pro/page/3/
    @GET
    fun getAlbums(@Url href: String): Observable<String>

    @GET
    fun getPhotos(@Url href: String): Observable<String>


}