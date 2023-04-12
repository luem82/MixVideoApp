package com.example.mixvideoapp.model

import java.io.Serializable

data class MixAlbum(
    var title: String,
    var thumb: String,
    var href: String,
    var count: String,
    var type: String
) : Serializable