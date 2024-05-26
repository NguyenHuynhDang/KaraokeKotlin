package com.example.karaokekotlin.model


import com.google.gson.annotations.SerializedName

data class SongResponse(
    @SerializedName("items")
    val items: List<Item>,
)