package com.example.karaokekotlin.data

import com.example.karaokekotlin.data.database.LocalDataSource
import javax.inject.Inject

class Repository @Inject constructor(
    remoteDataSource: RemoteDataSource,
    localDataSource: LocalDataSource
){
    val remote = remoteDataSource
    val local = localDataSource
}