package com.example.animaltube.network

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

object NetworkBasic {
    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36"
    val HTTP_CLIENT = OkHttpClient.Builder().addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .url(original.url())
            .header("User-Agent", USER_AGENT)
            .build()
        chain.proceed(request)
    }.cookieJar(object : CookieJar {
        private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val cookies = cookieStore[url]
            Log.e("NetworkBasic", "loadForRequest: $url")
            return cookies ?: ArrayList()
        }
    }).build()

    fun getRetrofit(baseUrl: String?): Retrofit {
        return Retrofit.Builder()
            .client(HTTP_CLIENT)
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }
}