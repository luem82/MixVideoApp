package com.example.mixvideoapp.model

import java.io.Serializable

data class MixPhoto(
    var small: String,
    var big: String,
    var type: String
) : Serializable