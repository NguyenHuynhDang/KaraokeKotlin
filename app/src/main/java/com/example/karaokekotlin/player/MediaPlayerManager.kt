package com.example.karaokekotlin.player

import android.media.MediaPlayer
import java.io.IOException

object MediaPlayerManager {
    var mediaPlayer: MediaPlayer? = MediaPlayer()

    fun playSong(url: String, onPreparedListener: MediaPlayer.OnPreparedListener? = null, onCompletionListener: MediaPlayer.OnCompletionListener? = null) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener {
                it.start()
                onPreparedListener?.onPrepared(it)
            }
            mediaPlayer?.setOnCompletionListener {
                onCompletionListener?.onCompletion(it)
            }
            mediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopPlaying() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
}