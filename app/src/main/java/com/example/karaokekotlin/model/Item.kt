package com.example.karaokekotlin.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Item(
    @SerializedName("etag")
    val etag: String,
    @SerializedName("id")
    val id: @RawValue Id,
    @SerializedName("kind")
    val kind: String,
    @SerializedName("snippet")
    val snippet: @RawValue Snippet
): Parcelable