package com.example.karaokekotlin.bindingadapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.karaokekotlin.R
import com.example.karaokekotlin.model.Item
import com.example.karaokekotlin.model.SongResponse
import com.example.karaokekotlin.ui.SongListFragmentDirections
import com.example.karaokekotlin.util.NetworkResult

class SongBinding {
    companion object {
        @BindingAdapter("readApiResponse", requireAll = true)
        @JvmStatic
        fun errorImageViewVisibility(
            imageView: ImageView,
            apiResponse: NetworkResult<SongResponse>?
        ) {
            when (apiResponse) {
                is NetworkResult.Error -> imageView.visibility = View.VISIBLE
                is NetworkResult.Loading -> imageView.visibility = View.INVISIBLE
                else ->
                imageView.visibility = View.INVISIBLE
            }
        }

        @BindingAdapter("readApiResponse2", requireAll = true)
        @JvmStatic
        fun errorTextViewVisibility(
            textView: TextView,
            apiResponse: NetworkResult<SongResponse>?
        ) {
            if (apiResponse is NetworkResult.Error) {
                textView.visibility = View.VISIBLE
                textView.text = apiResponse.message.toString()
            } else if (apiResponse is NetworkResult.Loading) {
                textView.visibility = View.INVISIBLE
            } else if (apiResponse is NetworkResult.Success) {
                textView.visibility = View.INVISIBLE
            }
        }

        @BindingAdapter("readApiResponse3", requireAll = true)
        @JvmStatic
        fun rvViewVisibility(
            view: RecyclerView,
            apiResponse: NetworkResult<SongResponse>?
        ) {
            if (apiResponse is NetworkResult.Error) {
                view.visibility = View.INVISIBLE
            } else if (apiResponse is NetworkResult.Loading) {
                view.visibility = View.INVISIBLE
            } else if (apiResponse is NetworkResult.Success) {
                view.visibility = View.VISIBLE
            }
        }

        @BindingAdapter("loadImageFromUrl")
        @JvmStatic
        fun loadImageFormUrl(imageView: ImageView, imageUrl: String) {
            imageView.load(imageUrl) {
                crossfade(600)
                error(R.drawable.baseline_browser_not_supported_24)
            }
        }

        @BindingAdapter("onSongClicked")
        @JvmStatic
        fun onSongSelected(view: ConstraintLayout, item: Item) {
            view.setOnClickListener {
                try {
                    val action = SongListFragmentDirections.actionSongListFragmentToDetailActivity(item)
                    view.findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.d("onSongClicked", e.message.toString())
                }
            }
        }
    }
}