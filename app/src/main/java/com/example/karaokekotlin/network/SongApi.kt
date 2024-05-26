package com.example.karaokekotlin.network

import com.example.karaokekotlin.model.SongResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SongApi {
    @GET("/youtube/v3/search")
    suspend fun getSongs(
        @QueryMap queries: Map<String, String>
    ) : Response<SongResponse>

    @GET("/youtube/v3/search")
    suspend fun searchSongs(
        @QueryMap queries: Map<String, String>
    ) : Response<SongResponse>
}