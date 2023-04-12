package com.example.mixvideoapp.allxinfo

import java.io.Serializable

data class AllXVideo(
    val title: String,
    val thumb: String,
    val views: String,
    val rates: String,
    val duration: String,
    val date: String,
    val href: String,
    val embed: String,
    val trailer: String,
) : Serializable
