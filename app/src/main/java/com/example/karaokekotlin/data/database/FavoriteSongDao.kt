package com.example.karaokekotlin.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(favoriteSongsEntity: FavoriteSongEntity)

    @Query("SELECT * FROM favorite_songs_table ORDER BY id ASC")
    fun readFavoriteSongs(): Flow<List<FavoriteSongEntity>>

    @Update
    suspend fun updateFavoriteSong(favoriteSongsEntity: FavoriteSongEntity)

    @Delete
    suspend fun deleteFavoriteSong(favoriteSongsEntity: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs_table")
    suspend fun deleteAllFavoriteSongs()
}