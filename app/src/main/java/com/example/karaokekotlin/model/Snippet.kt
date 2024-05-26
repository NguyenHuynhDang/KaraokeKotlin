package com.example.karaokekotlin.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Snippet(
    @SerializedName("channelTitle")
    val channelTitle: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("thumbnails")
    val thumbnails: @RawValue Thumbnails,
    @SerializedName("title")
    var title: String
): Parcelable