package com.example.karaokekotlin.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.karaokekotlin.model.Item
import com.example.karaokekotlin.util.Constants.Companion.FAVORITE_SONG_TABLE

@Entity(tableName = FAVORITE_SONG_TABLE)
data class FavoriteSongEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var item: Item
)