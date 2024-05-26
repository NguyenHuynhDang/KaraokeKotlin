package com.example.karaokekotlin.data

import com.example.karaokekotlin.model.SongResponse
import com.example.karaokekotlin.network.SongApi
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val songApi: SongApi
){
    suspend fun getSongResponse(queries: Map<String, String>): Response<SongResponse> {
        return songApi.getSongs(queries)
    }

    suspend fun searchSongResponse(queries: Map<String, String>): Response<SongResponse> {
        return songApi.searchSongs(queries)
    }
}