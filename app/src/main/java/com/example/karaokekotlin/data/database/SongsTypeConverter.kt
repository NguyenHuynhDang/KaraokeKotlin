package com.example.karaokekotlin.data.database

import androidx.room.TypeConverter
import com.example.karaokekotlin.model.Item
import com.example.karaokekotlin.model.SongResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SongsTypeConverter {
    private var gson = Gson()

    @TypeConverter
    fun songToString(song: Item): String {
        return gson.toJson(song)
    }

    @TypeConverter
    fun stringToSong(data: String): Item {
        val listType = object : TypeToken<Item>() {}.type
        return gson.fromJson(data, listType)
    }
}
