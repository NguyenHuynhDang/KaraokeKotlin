package com.example.karaokekotlin.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Thumbnails(
    @SerializedName("medium")
    val medium: @RawValue Medium
) : Parcelable