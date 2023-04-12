package com.example.mixvideoapp.model

import java.io.Serializable

data class MixVideo(
    var title: String,
    var thumb: String,
    var duration: String,
    var href: String,
    var preview: String,
    var type: String,
    var link: String? = null,
    var time: String? = null
) : Serializable