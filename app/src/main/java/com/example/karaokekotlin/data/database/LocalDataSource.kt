package com.example.karaokekotlin.data.database

import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao,
    private val recordedSongDao: RecordedSongDao
){

    //favorite
    fun readFavoriteSongs(): Flow<List<FavoriteSongEntity>> {
        return favoriteSongDao.readFavoriteSongs()
    }

    suspend fun insertFavoriteSong(favoritesEntity: FavoriteSongEntity) {
        favoriteSongDao.insertFavoriteSong(favoritesEntity)
    }

    suspend fun updateFavoriteSong(favoritesEntity: FavoriteSongEntity) {
        favoriteSongDao.updateFavoriteSong(favoritesEntity)
    }

    suspend fun deleteFavoriteSong(favoritesEntity: FavoriteSongEntity) {
        favoriteSongDao.deleteFavoriteSong(favoritesEntity)
    }

    suspend fun deleteAllFavoriteSongs() {
        favoriteSongDao.deleteAllFavoriteSongs()
    }

    //recorded
    fun readRecordedSongs(): Flow<List<RecordedSongEntity>> {
        return recordedSongDao.readRecordedSongs()
    }

    suspend fun insertRecordedSong(recordedSongEntity: RecordedSongEntity) {
        recordedSongDao.insertRecordedSong(recordedSongEntity)
    }

    suspend fun updateRecordedSong(recordedSongEntity: RecordedSongEntity) {
        recordedSongDao.updateRecordedSong(recordedSongEntity)
    }

    suspend fun deleteRecordedSong(recordedSongEntity: RecordedSongEntity) {
        recordedSongDao.deleteRecordedSong(recordedSongEntity)
    }

    suspend fun deleteAllRecordedSongs() {
        recordedSongDao.deleteAllRecordedSongs()
    }
}