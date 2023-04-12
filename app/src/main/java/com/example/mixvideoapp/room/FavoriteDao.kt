package com.example.mixvideoapp.room

import androidx.room.*
import com.example.mixvideoapp.model.MixVideo

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM Table_Video_Favorite ORDER BY id DESC")
    fun getFavoriteVideos(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavoriteVideo(meVideo: Favorite)

    @Delete
    fun deleteFavoriteVideo(meVideo: Favorite)

    @Update
    fun updateFavoriteVideo(meVideo: Favorite)

    @Query("DELETE FROM Table_Video_Favorite")
    fun clearAllFavorite()
}