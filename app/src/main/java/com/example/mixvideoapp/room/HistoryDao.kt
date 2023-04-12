package com.example.mixvideoapp.room

import androidx.room.*
import com.example.mixvideoapp.model.MixVideo

@Dao
interface HistoryDao {

    @Query("SELECT * FROM Table_Video_History ORDER BY id DESC")
    fun getHistoryVideos(): List<History>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHistoryVideo(meVideo: History)

    @Delete
    fun deleteHistoryVideo(meVideo: History)

    @Update
    fun updateHistoryVideo(meVideo: History)

    @Query("DELETE FROM Table_Video_History")
    fun clearAllHistory()
}