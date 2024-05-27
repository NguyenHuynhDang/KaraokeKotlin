package com.example.karaokekotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.karaokekotlin.databinding.SongRowLayoutBinding
import com.example.karaokekotlin.model.Item
import com.example.karaokekotlin.model.SongResponse
import com.example.karaokekotlin.ui.SearchFragmentDirections
import com.example.karaokekotlin.ui.SongListFragmentDirections
import com.example.karaokekotlin.util.SongsDiffUtil
import com.example.karaokekotlin.viewmodel.MainViewModel

class SearchAdapter(
    private val mainViewModel: MainViewModel
) : RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {
    private var songs = emptyList<Item>()

    class MyViewHolder(
        val binding: SongRowLayoutBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(items: Item) {
            binding.item = items
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SongRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentSong = songs[position]
        holder.bind(currentSong)
        holder.binding.songLayout.setOnClickListener {
            if (mainViewModel.networkStatus) {
                val action = SearchFragmentDirections.actionSearchFragmentToDetailActivity(currentSong)
                holder.binding.songLayout.findNavController().navigate(action)
            } else {
                mainViewModel.showNetworkStatus()
            }
        }
    }

    fun setData(newData: SongResponse) {
        val recipesDiffUtil = SongsDiffUtil(songs, newData.items)
        val diffUtilResult = DiffUtil.calculateDiff(recipesDiffUtil)
        songs = newData.items
        diffUtilResult.dispatchUpdatesTo(this)
    }
}