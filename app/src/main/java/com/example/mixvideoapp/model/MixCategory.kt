package com.example.mixvideoapp.model

import java.io.Serializable

data class MixCategory(
    val title: String,
    val thumb: String,
    val href: String,
    val type: String,
    val max: Int
) : Serializable
