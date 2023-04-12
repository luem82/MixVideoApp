package com.example.mixvideoapp.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mixvideoapp.room.Favorite
import com.example.mixvideoapp.room.FavoriteDao
import com.example.mixvideoapp.room.History
import com.example.mixvideoapp.room.HistoryDao


@Database(entities = arrayOf(History::class, Favorite::class), version = 1, exportSchema = false)
abstract class MixRoomDatabase : RoomDatabase() {
    abstract fun getFavoriteDao(): FavoriteDao
    abstract fun getHistoryDao(): HistoryDao
}