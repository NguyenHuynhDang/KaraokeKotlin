package com.example.karaokekotlin.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Id(
    @SerializedName("videoId")
    val videoId: String
) : Parcelable