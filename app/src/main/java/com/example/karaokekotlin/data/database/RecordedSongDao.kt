package com.example.karaokekotlin.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordedSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecordedSong(recordedSongEntity: RecordedSongEntity)

    @Query("SELECT * FROM recorded_song_table ORDER BY id ASC")
    fun readRecordedSongs(): Flow<List<RecordedSongEntity>>

    @Update
    suspend fun updateRecordedSong(recordedSongEntity: RecordedSongEntity)

    @Delete
    suspend fun deleteRecordedSong(recordedSongEntity: RecordedSongEntity)

    @Query("DELETE FROM recorded_song_table")
    suspend fun deleteAllRecordedSongs()
}
