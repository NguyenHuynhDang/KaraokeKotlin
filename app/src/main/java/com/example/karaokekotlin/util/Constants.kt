package com.example.karaokekotlin.util

class Constants {
    companion object {
        const val BASE_URL = "https://www.googleapis.com"
        const val FAVORITE_SONG_TABLE = "favorite_songs_table"
        const val DATABASE_NAME = "favorite_songs_database"
        const val RECORDED_SONG_TABLE = "recorded_song_table"


        // queries

        const val API_KEY = "AIzaSyBdABdlzLZjaZ16ef97BhxQeZPuKr5Mk8o"
        const val DEFAULT_CONTENT = "karaoke"
        const val DEFAULT_MAX = "20"
        const val DEFAULT_QUERY_PART = "snippet"
        const val DEFAULT_QUERY_TYPE = "video"

        const val QUERY_PART = "part"
        const val QUERY_TYPE = "type"
        const val QUERY_CONTENT = "q"
        const val QUERY_API_KEY = "key"
        const val QUERY_MAX_RESULT = "maxResults"

        //AudioCaptureService
        const val LOG_TAG = "AudioCaptureService"
        const val SERVICE_ID = 123
        const val NOTIFICATION_CHANNEL_ID = "AudioCapture channel"

        const val NUM_SAMPLES_PER_READ = 1024

        private const val BYTES_PER_SAMPLE = 2 // 2 bytes since we hardcoded the PCM 16-bit format
        const val BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE
        const val AUDIO_CAPTURE_SAMPLE_RATE = 44100

        const val ACTION_START = "AudioCaptureService:Start"
        const val ACTION_STOP = "AudioCaptureService:Stop"
        const val EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData"

        const val DES_PATH_KEY = "des_path_key"
        val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
        val MEDIA_PROJECTION_REQUEST_CODE = 13

        //AudioEncode
        const val AUDIO_CHANNELS = 2
        const val BIT_RATE = 128000
        const val SAMPLE_RATE = 22000
    }
}