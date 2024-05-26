package com.example.karaokekotlin.player

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}