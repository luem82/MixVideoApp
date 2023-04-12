package com.example.mixvideoapp.interfaces

import com.example.mixvideoapp.model.MixVideo
import com.example.mixvideoapp.room.Favorite


interface IVideoFavoriteListener {
    fun onRemoveToFavorite(favorite: Favorite)
}