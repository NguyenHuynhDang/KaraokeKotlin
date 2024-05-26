package com.example.karaokekotlin.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.karaokekotlin.data.Repository
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity
import com.example.karaokekotlin.model.SongResponse
import com.example.karaokekotlin.util.Constants
import com.example.karaokekotlin.util.NetworkResult
import com.example.karaokekotlin.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import linc.com.pcmdecoder.PCMDecoder
import retrofit2.Response
import java.io.File
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    var networkStatus = false
    /** ROOM DATABASE */
    //favorite songs
    val readFavoriteSongs: LiveData<List<FavoriteSongEntity>> = repository.local.readFavoriteSongs().asLiveData()
    fun insertFavoriteSong(favoriteSong: FavoriteSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFavoriteSong(favoriteSong)
        }

    fun updateFavoriteSong(favoriteSong: FavoriteSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.updateFavoriteSong(favoriteSong)
        }

    fun deleteFavoriteSong(favoritesEntity: FavoriteSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteFavoriteSong(favoritesEntity)
        }

    fun deleteAllFavoriteSongs() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllFavoriteSongs()
        }
    //recorded songs
    val readRecordedSongs: LiveData<List<RecordedSongEntity>> = repository.local.readRecordedSongs().asLiveData()
    private fun insertRecordedSong(recordedSongEntity: RecordedSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertRecordedSong(recordedSongEntity)
        }

    fun updateRecordedSong(recordedSongEntity: RecordedSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.updateRecordedSong(recordedSongEntity)
        }

    fun deleteRecordedSong(recordedSongEntity: RecordedSongEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteRecordedSong(recordedSongEntity)
        }

    fun deleteAllRecordedSongs() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllRecordedSongs()
        }

    /** RETROFIT */
    var defaultSongResponse: MutableLiveData<NetworkResult<SongResponse>> = MutableLiveData()
    var searchSongResponse: MutableLiveData<NetworkResult<SongResponse>> = MutableLiveData()

    fun getDefaultSongResponse(queries: Map<String, String>) = viewModelScope.launch {
        getDefaultSongResponseSafeCall(queries)
    }

    fun getSearchSongResponse(queries: Map<String, String>) = viewModelScope.launch {
        getSearchSongResponseSafeCall(queries)
    }

    private suspend fun getSearchSongResponseSafeCall(queries: Map<String, String>) {
        searchSongResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.searchSongResponse(queries)
                searchSongResponse.value = handleSongResponse(response)
            } catch (e: Exception) {
                searchSongResponse.value = NetworkResult.Error("Songs not found.")
            }
        } else {
            searchSongResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun getDefaultSongResponseSafeCall(queries: Map<String, String>) {
        defaultSongResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getSongResponse(queries)
                defaultSongResponse.value = handleSongResponse(response)
            } catch (e: Exception) {
                defaultSongResponse.value = NetworkResult.Error("Songs not found.")
            }
        } else {
            defaultSongResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private fun handleSongResponse(response: Response<SongResponse>): NetworkResult<SongResponse> {
        return when {
            response.body()!!.items.isEmpty() -> {
                NetworkResult.Error(response.message())
            }

            response.isSuccessful -> {
                val songsModel = response.body()
                NetworkResult.Success(songsModel!!)
            }

            else -> {
                NetworkResult.Error(response.message())
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun showNetworkStatus() {
        if (!networkStatus) {
            Toast.makeText(getApplication(), "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }
    //save recorded song
    suspend fun saveRecordedSongToDatabase(pcmPath: String, newName: String) {
        val mp3File = File(pcmPath.replace(".pcm", ".mp3"))
        Log.d("TAGGGggg", mp3File.path)
        withContext(Dispatchers.IO) {
            pcmToMp3(File(pcmPath), mp3File)
        }
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(mp3File.path)
        val dur =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
        //val durationInInt: Int = (dur/1000).toInt()
        Log.d("TAGggg mp3", mp3File.name)
        Log.d("TAGggg mp3", mp3File.path)
        Log.d("TAGggg mp3", mp3File.length().toString())
        insertRecordedSong(
            RecordedSongEntity(
                0,
                newName,
                mp3File.path,
                dur
            )
        )
    }

    //pcm to mp3 convert
    private fun pcmToMp3(pcmFile: File, mp3File: File) {
        //viewModelScope.launch {
            PCMDecoder.encodeToMp3(
                pcmFile.path,
                Constants.AUDIO_CHANNELS,
                Constants.BIT_RATE,
                Constants.SAMPLE_RATE,
                mp3File.path
            )
        //}
    }
}