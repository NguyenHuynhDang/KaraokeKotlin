package com.example.karaokekotlin.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.karaokekotlin.R
import com.example.karaokekotlin.data.database.entities.RecordedSongEntity
import com.example.karaokekotlin.databinding.RecordedSongRowLayoutBinding
import com.example.karaokekotlin.player.MediaPlayerManager
import com.example.karaokekotlin.util.SongsDiffUtil
import com.example.karaokekotlin.util.Utils
import com.example.karaokekotlin.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File

class RecordedSongAdapter (
    private val requireActivity: FragmentActivity,
    private val mainViewModel: MainViewModel,
    private val mediaPlayerManager: MediaPlayerManager
) : RecyclerView.Adapter<RecordedSongAdapter.MyViewHolder>(), ActionMode.Callback {
    private var multiSelection = false

    private lateinit var mActionMode: ActionMode
    private lateinit var rootView: View
    private lateinit var contextMenu: Menu

    private var selectedItems = arrayListOf<RecordedSongEntity>()
    private var myViewHolders = arrayListOf<MyViewHolder>()
    private var recordedSongs = emptyList<RecordedSongEntity>()

    var currentPlayingPosition: Int = -1
    private var seekBarUpdateHandler = Handler(Looper.getMainLooper())
    private var seekBarUpdater: Runnable? = null

    inner class MyViewHolder(val binding: RecordedSongRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val seekBar: SeekBar = binding.sbRecordSb

        fun bind(recordedSongEntity: RecordedSongEntity, position: Int, holder: MyViewHolder) {
            binding.recordedSongEntity = recordedSongEntity
            binding.util = Utils
            val currentTimeTextView = binding.tvCurrentTime
            binding.executePendingBindings()
            binding.recordedSongRowLayout.setOnClickListener {
                if (multiSelection) {
                    applySelection(holder, recordedSongEntity)
                } else {
                    if (currentPlayingPosition == position) {
                        mediaPlayerManager.stopPlaying()
                        seekBar.progress = 0
                        currentTimeTextView.text = "00:00"
                        currentPlayingPosition = -1
                        seekBarUpdater?.let { seekBarUpdateHandler.removeCallbacks(it) }
                    } else {
                        currentTimeTextView.visibility = View.VISIBLE
                        seekBar.visibility = View.VISIBLE
                        mediaPlayerManager.playSong(recordedSongEntity.path, {
                            seekBar.max = it.duration
                            startUpdatingSeekBar(seekBar, currentTimeTextView)
                        }, {
                            seekBar.progress = 0
                            //
                            currentTimeTextView.text = "00:00"
                            currentPlayingPosition = -1
                            //
                            seekBarUpdater?.let { seekBarUpdateHandler.removeCallbacks(it) }
                        })
                        currentPlayingPosition = position
                    }
                }

            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayerManager.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }


    private fun startUpdatingSeekBar(seekBar: SeekBar, tv: TextView) {
        seekBarUpdater?.let { seekBarUpdateHandler.removeCallbacks(it) }

        seekBarUpdater = object : Runnable {
            override fun run() {
                mediaPlayerManager.mediaPlayer?.let {
                    seekBar.progress = it.currentPosition
                    tv.text = Utils.msToDuration(seekBar.progress.toLong())
                    if (it.isPlaying) {
                        seekBarUpdateHandler.postDelayed(this, 1000)
                    }
                }
            }
        }

        seekBarUpdateHandler.post(seekBarUpdater!!)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecordedSongRowLayoutBinding.inflate(layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        myViewHolders.add(holder)
        rootView = holder.itemView.rootView

        val currentSong = recordedSongs[position]
        holder.bind(currentSong, position, holder)

        saveItemStateOnScroll(currentSong, holder)

        /**
         * Single Click Listener
         * */
//        holder.binding.recordedSongRowLayout.setOnClickListener {
//            if (multiSelection) {
//                applySelection(holder, currentSong)
//            }
//        }

        /**
         * Long Click Listener
         * */
        holder.binding.recordedSongRowLayout.setOnLongClickListener {
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

    private fun saveItemStateOnScroll(currentSong: RecordedSongEntity, holder: MyViewHolder){
        if (selectedItems.contains(currentSong)) {
            changeSongStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
        } else {
            changeSongStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
        }
    }

    private fun applySelection(holder: MyViewHolder, currentSong: RecordedSongEntity) {
        if (selectedItems.contains(currentSong)) {
            selectedItems.remove(currentSong)
            changeSongStyle(holder, R.color.cardBackgroundColor, R.color.strokeColor)
            applyActionModeTitle()
        } else {
            selectedItems.add(currentSong)
            changeSongStyle(holder, R.color.cardBackgroundLightColor, R.color.colorPrimary)
            applyActionModeTitle()
        }
    }

    private fun changeSongStyle(holder: MyViewHolder, backgroundColor: Int, strokeColor: Int) {
        holder.binding.recordedSongRowLayout.setBackgroundColor(
            ContextCompat.getColor(requireActivity, backgroundColor)
        )
        holder.binding.recordedSongRowCardView.strokeColor =
            ContextCompat.getColor(requireActivity, strokeColor)
    }

    private fun applyActionModeTitle() {
        when (selectedItems.size) {
            0 -> {
                mActionMode.finish()
                multiSelection = false
            }
            1 -> {
                mActionMode.title = "${selectedItems.size} ${ContextCompat.getString(requireActivity, R.string.item_selected)}"
                contextMenu.findItem(R.id.rename_favorite_song_menu).isVisible = true
            }
            else -> {
                mActionMode.title = "${selectedItems.size} ${ContextCompat.getString(requireActivity, R.string.item_selected)}"
                contextMenu.findItem(R.id.rename_favorite_song_menu).isVisible = false
            }
        }
    }

    override fun getItemCount(): Int {
        return recordedSongs.size
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
            selectedItems.forEach {
                val file = File(it.path)
                if (file.exists()) file.delete()
                mainViewModel.deleteRecordedSong(it)
            }
            showSnackBar("${ContextCompat.getString(requireActivity, R.string.delete)} ${selectedItems.size} ${ContextCompat.getString(requireActivity, R.string.item)}.")

            multiSelection = false
            selectedItems.clear()
            actionMode?.finish()
        } else if (menu?.itemId == R.id.rename_favorite_song_menu) {
            //rename dialog
            val view = requireActivity.layoutInflater.inflate(R.layout.dialog_layout, null)
            val editText: EditText = view.findViewById(R.id.etSongName)
            view.findViewById<TextView>(R.id.dialog_title).text = requireActivity.getString(R.string.recorded_rename)
            editText.setText(selectedItems.first().name)
            MaterialAlertDialogBuilder(requireActivity).apply {
                setView(view)
                setNegativeButton(requireActivity.getString(R.string.cancel)) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                setPositiveButton(requireActivity.getString(R.string.save)) { _, _ ->
                    mainViewModel.updateRecordedSong(selectedItems.first().copy(name = editText.text.toString()))
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
        selectedItems.clear()
        applyStatusBarColor(R.color.statusBarColor)
    }

    private fun applyStatusBarColor(color: Int) {
        requireActivity.window.statusBarColor =
            ContextCompat.getColor(requireActivity, color)
    }

    fun setData(newFavoriteSong: List<RecordedSongEntity>) {
        val favoriteRecipesDiffUtil =
            SongsDiffUtil(recordedSongs, newFavoriteSong)
        val diffUtilResult = DiffUtil.calculateDiff(favoriteRecipesDiffUtil)
        recordedSongs = newFavoriteSong
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