package com.example.karaokekotlin.stream

class StreamObject(
    private val sampleRate: Int,
    private val bufferSize: Int
) {
    fun startStreaming() {
        stream(sampleRate, bufferSize)
    }

    private external fun stream(sampleRate: Int, bufferSize: Int)
    external fun setEffectEnable(value: Boolean)
    external fun setAutoTuneEnable(value: Boolean)
    external fun setEffectValue(effectType: Int, value: Int)
    external fun setMicVolume(value: Float)
    external fun stopStreaming()
}