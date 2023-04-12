package com.example.mixvideoapp.pornogids

import java.io.Serializable

data class DetailGidsVideo(
    val source: String,
    val date: String,
    val desciption: String,
    val listRelated: List<GidsVideo>
) : Serializable
