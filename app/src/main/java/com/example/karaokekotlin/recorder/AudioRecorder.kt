package com.example.karaokekotlin.recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}