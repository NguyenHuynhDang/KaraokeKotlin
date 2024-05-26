package com.example.karaokekotlin.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity

@Database(
    entities = [FavoriteSongEntity::class, RecordedSongEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(SongsTypeConverter::class)
abstract class FavoriteSongDatabase :  RoomDatabase() {
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun recordedSongDao(): RecordedSongDao
}