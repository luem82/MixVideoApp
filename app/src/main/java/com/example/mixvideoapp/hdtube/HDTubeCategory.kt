package com.example.mixvideoapp.hdtube

import java.io.Serializable

data class HDTubeCategory(
    val title: String,
    val thumb: String,
    val count: String,
    val key: String,
    val href: String
) : Serializable
