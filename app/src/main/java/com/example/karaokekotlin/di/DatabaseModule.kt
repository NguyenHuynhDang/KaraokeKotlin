package com.example.karaokekotlin.di

import android.content.Context
import androidx.room.Room
import com.example.karaokekotlin.data.database.FavoriteSongDao
import com.example.karaokekotlin.data.database.FavoriteSongDatabase
import com.example.karaokekotlin.data.database.RecordedSongDao
import com.example.karaokekotlin.util.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        FavoriteSongDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: FavoriteSongDatabase): FavoriteSongDao = database.favoriteSongDao()

    @Singleton
    @Provides
    fun provideRecordedSongDao(database: FavoriteSongDatabase): RecordedSongDao = database.recordedSongDao()
}