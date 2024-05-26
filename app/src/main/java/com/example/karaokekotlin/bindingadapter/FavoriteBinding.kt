package com.example.karaokekotlin.bindingadapter

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.karaokekotlin.adapter.FavoriteSongAdapter
import com.example.karaokekotlin.adapter.RecordedSongAdapter
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity

class FavoriteBinding {
    companion object {

        @BindingAdapter("setVisibility", "setData", requireAll = false)
        @JvmStatic
        fun setVisibility(view: View, favoritesEntity: List<FavoriteSongEntity>?, mAdapter: FavoriteSongAdapter?) {
            when (view) {
                is RecyclerView -> {
                    val dataCheck = favoritesEntity.isNullOrEmpty()
                    view.isInvisible = dataCheck
                    if(!dataCheck){
                        favoritesEntity?.let { mAdapter?.setData(it) }
                    }
                }
                else -> view.isVisible = favoritesEntity.isNullOrEmpty()
            }
        }

        @BindingAdapter("setVisibility2", "setData2", requireAll = false)
        @JvmStatic
        fun setVisibility2(view: View, recordedSongEntity: List<RecordedSongEntity>?, mAdapter: RecordedSongAdapter?) {
            when (view) {
                is RecyclerView -> {
                    val dataCheck = recordedSongEntity.isNullOrEmpty()
                    view.isInvisible = dataCheck
                    if(!dataCheck){
                        recordedSongEntity?.let { mAdapter?.setData(it) }
                    }
                }
                else -> view.isVisible = recordedSongEntity.isNullOrEmpty()
            }
        }

    }
}