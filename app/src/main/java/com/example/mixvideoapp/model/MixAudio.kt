package com.example.mixvideoapp.model

import java.io.Serializable

data class MixAudio(
    val title: String,
    val thumb: String,
    val href: String,
    val date: String,
    val views: String
) : Serializable
