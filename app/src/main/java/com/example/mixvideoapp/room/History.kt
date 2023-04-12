package com.example.mixvideoapp.room

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


//@Entity(tableName = "Table_Video", indices = @Index(value = {"title"}, unique = true)) -> java
@Entity(
    tableName = "Table_Video_History",
    indices = [Index(value = ["title", "thumb", "history"], unique = true)]
)
data class History(

    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "thumb")
    var thumb: String,

    @ColumnInfo(name = "duration")
    var duration: String,

    @ColumnInfo(name = "href")
    var href: String,

    @ColumnInfo(name = "preview")
    var preview: String,

    @ColumnInfo(name = "type")
    var type: String,

    @ColumnInfo(name = "history")
    var history: Boolean,

    @ColumnInfo(name = "current")
    var current: Long = 0,

    @ColumnInfo(name = "percent")
    var percent: Int = 0
)