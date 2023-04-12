package com.example.mixvideoapp.pornogids

import java.io.Serializable

data class GidsCategory(
    val title: String,
    val thumb: String,
    val href: String,
    val key: String,
    val count: String,
    val rates: String
) : Serializable
