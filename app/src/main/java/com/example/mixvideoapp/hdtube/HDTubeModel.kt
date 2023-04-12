package com.example.mixvideoapp.hdtube

import java.io.Serializable

data class HDTubeModel(
    val name: String,
    val thumb: String,
    val href: String
) : Serializable
