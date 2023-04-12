package com.example.mixvideoapp.hdtube

import java.io.Serializable

data class HDTubeVideo(
    val title: String,
    val thumb: String,
    val duration: String,
    val trailer: String,
    val href: String
) : Serializable
