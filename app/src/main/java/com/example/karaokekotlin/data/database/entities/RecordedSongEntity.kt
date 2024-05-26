package com.example.karaokekotlin.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.karaokekotlin.util.Constants.Companion.RECORDED_SONG_TABLE

@Entity(tableName = RECORDED_SONG_TABLE)
data class RecordedSongEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var path: String,
    var duration: Long
)