package com.example.mixvideoapp.interfaces

import com.example.mixvideoapp.room.History


interface IVideoHistoryListener {
    fun onRemoveToHistory(history: History)
}