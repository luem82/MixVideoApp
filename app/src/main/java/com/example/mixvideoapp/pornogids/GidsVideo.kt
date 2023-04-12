package com.example.mixvideoapp.pornogids

import java.io.Serializable

data class GidsVideo(
    val title: String,
    val thumb: String,
    val href: String,
    val views: String,
    val rates: String,
    val duration: String
) : Serializable
