package com.example.mixvideoapp.allxinfo

import java.io.Serializable

data class InfoDetail(
    val videoTitle: String,
    val videoDescription: String,
    val videoSource: String,
    val listRelated: List<AllXVideo>
) : Serializable