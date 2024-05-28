package com.example.karaokekotlin.adapter

import android.app.ProgressDialog.show
import android.view.ActionMode
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.karaokekotlin.R
import com.example.karaokekotlin.data.database.entities.FavoriteSongEntity
import com.example.karaokekotlin.databinding.FavoriteSongRowLayoutBinding
import com.example.karaokekotlin.ui.FavoriteFragmentDirections
import com.example.karaokekotlin.util.SongsDiffUtil
import com.example.karaokekotlin.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.android.material.snackbar.Snackbar
import java.io.File

class FavoriteSongAdapter(
    private val requireActivity: FragmentActivity,
    private val mainViewModel: MainViewModel
) : RecyclerView.Adapter<FavoriteSongAdapter.MyViewHolder>(), ActionMode.Callback {

    private var multiSelection = false

    private lateinit var mActionMode: ActionMode
    private lateinit var rootView: View
    private lateinit var contextMenu: Menu

    private var selectedSongs = arrayListOf<FavoriteSongEntity>()
    private var myViewHolders = arrayListOf<MyViewHolder>()
    private var favoriteSongs = emptyList<FavoriteSongEntity>()

    class MyViewHolder(val binding: FavoriteSongRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favoriteSongEntity: FavoriteSongEntity) {
            binding.favoriteSongEntity = favoriteSongEntity
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FavoriteSongRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        myViewHolders.add(holder)
        rootView = holder.itemView.rootView

        val currentSong = favoriteSongs[position]
        holder.bind(currentSong)

        saveItemStateOnScroll(currentSong, holder)

        /**
         * Single Click Listener
         * */
        holder.binding.favoriteSongRowLayout.setOnClickListener {
            if (multiSelection) {
                applySelection(holder, currentSong)
            } else {
                if (mainViewModel.networkStatus) {
                    val action =
                        FavoriteFragmentDirections.actionFavoriteFragmentToDetailActivity(
                            currentSong.item
                        )
                    holder.itemView.findNavController().navigate(action)
                } else {
                    Toast.makeText(requireActivity, ContextCompat.getString(requireActivity, R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * Long Click Listener
         * */
        holder.binding.favoriteSongRowLayout.setOnLongClickListener {
            if (!multiSelection) {
                multiSelection = true
                requireActivity.startActionMode(this)
                applySelection(holder, currentSong)
                true
            } else {
                applySelection(holder, currentSong)
                true
            }

        }

    }

    private fun saveItemStateOnScroll(currentSong: FavoriteSongEntity, holder: MyViewHolder){
        if (selectedSongs.contains(currentSong)) {
            changeSongStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
        } else {
            changeSongStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        }
    }

    private fun applySelection(holder: MyViewHolder, currentSong: FavoriteSongEntity) {
        if (selectedSongs.contains(currentSong)) {
            selectedSongs.remove(currentSong)
            changeSongStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
            applyActionModeTitle()
        } else {
            selectedSongs.add(currentSong)
            changeSongStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
            applyActionModeTitle()
        }
    }

    private fun changeSongStyle(holder: MyViewHolder, backgroundColor: Int, strokeColor: Int) {
        holder.binding.favoriteSongRowLayout.setBackgroundColor(
            ContextCompat.getColor(requireActivity, backgroundColor)
        )
        holder.binding.favoriteRowCardView.strokeColor =
            ContextCompat.getColor(requireActivity, strokeColor)
    }

    private fun applyActionModeTitle() {
        when (selectedSongs.size) {
            0 -> {
                mActionMode.finish()
                multiSelection = false
            }
            1 -> {
                mActionMode.title = "${selectedSongs.size} ${ContextCompat.getString(requireActivity, R.string.item_selected)}"
                contextMenu.findItem(R.id.rename_favorite_song_menu).isVisible = true
            }
            else -> {
                mActionMode.title = "${selectedSongs.size} ${ContextCompat.getString(requireActivity, R.string.item_selected)}"
                contextMenu.findItem(R.id.rename_favorite_song_menu).isVisible = false
            }
        }
    }

    override fun getItemCount(): Int {
        return favoriteSongs.size
    }

    override fun onCreateActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
        actionMode?.menuInflater?.inflate(R.menu.favorite_contextual_menu, menu)
        mActionMode = actionMode!!
        contextMenu = menu!!
        applyStatusBarColor(R.color.contextualStatusBarColor)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(actionMode: ActionMode?, menu: MenuItem?): Boolean {
        if (menu?.itemId == R.id.delete_favorite_song_menu) {
            selectedSongs.forEach {
                mainViewModel.deleteFavoriteSong(it)
            }
            showSnackBar("${ContextCompat.getString(requireActivity, R.string.delete)} ${selectedSongs.size} ${ContextCompat.getString(requireActivity, R.string.item)}.")

            multiSelection = false
            selectedSongs.clear()
            actionMode?.finish()
        } else if (menu?.itemId == R.id.rename_favorite_song_menu) {
            val newFavoriteSong = selectedSongs.first().copy()
            val view = requireActivity.layoutInflater.inflate(R.layout.dialog_layout, null)
            val editText: EditText = view.findViewById(R.id.etSongName)
            view.findViewById<TextView>(R.id.dialog_title).text = requireActivity.getString(R.string.song_rename)
            editText.setText(newFavoriteSong.item.snippet.title)
            MaterialAlertDialogBuilder(requireActivity).apply {
                setView(view)
                setNegativeButton(requireActivity.getString(R.string.cancel)) { dialogInterface, _ ->
                    dialogInterface.cancel()
                    actionMode?.finish()
                }
                setPositiveButton(requireActivity.getString(R.string.save)) { _, _ ->
                    mainViewModel.updateFavoriteSong(newFavoriteSong.apply { item.snippet.title = editText.text.toString() })
                    actionMode?.finish()
                }
                show()
            }
        }
        return true
    }

    override fun onDestroyActionMode(actionMode: ActionMode?) {
        myViewHolders.forEach { holder ->
            changeSongStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        }
        multiSelection = false
        selectedSongs.clear()
        applyStatusBarColor(R.color.statusBarColor)
    }

    private fun applyStatusBarColor(color: Int) {
        requireActivity.window.statusBarColor =
            ContextCompat.getColor(requireActivity, color)
    }

    fun setData(newFavoriteSong: List<FavoriteSongEntity>) {
        val favoriteRecipesDiffUtil =
            SongsDiffUtil(favoriteSongs, newFavoriteSong)
        val diffUtilResult = DiffUtil.calculateDiff(favoriteRecipesDiffUtil)
        favoriteSongs = newFavoriteSong
        diffUtilResult.dispatchUpdatesTo(this)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            rootView,
            message,
            Snackbar.LENGTH_SHORT
        ).setAction("OK") {}
            .show()
    }

    fun clearContextualActionMode() {
        if (this::mActionMode.isInitialized) {
            mActionMode.finish()
        }
    }

}