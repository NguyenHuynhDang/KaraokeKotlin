package com.example.karaokekotlin.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.karaokekotlin.R
import com.example.karaokekotlin.adapter.RecordedSongAdapter
import com.example.karaokekotlin.databinding.FragmentRecordedBinding
import com.example.karaokekotlin.player.MediaPlayerManager
import com.example.karaokekotlin.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class RecordedFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var mAdapter: RecordedSongAdapter
    private var _binding: FragmentRecordedBinding? = null
    private val binding get() = _binding!!
    private val TAG = "TAGggg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "recorded frag oncreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("TAGggg", "recorded oncreateVIEW")
        _binding = FragmentRecordedBinding.inflate(inflater, container, false)
        mAdapter = RecordedSongAdapter(requireActivity(), mainViewModel, MediaPlayerManager)

        binding.lifecycleOwner = this
        binding.mainViewModel = mainViewModel
        binding.mAdapter = mAdapter

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.recorded_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.deleteAll_recorded_songs_menu) {
                    val listRecorded = mainViewModel.readRecordedSongs.value
                    for (r in listRecorded!!) {
                        val file = File(r.path)
                        if (file.exists()) {
                            file.delete()
                            Log.d("deletee", file.name)
                        }
                    }
                    mainViewModel.deleteAllRecordedSongs()
                    showSnackBar()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView(binding.recordedRecyclerView)

        return binding.root
    }
    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showSnackBar() {
        Snackbar.make(
            binding.root,
            ContextCompat.getString(requireContext(), R.string.all_songs_removed),
            Snackbar.LENGTH_SHORT
        ).setAction("Okay") {}
            .show()
    }

    override fun onDestroyView() {
        Log.d("TAGggg", "recorded ONDESTROYED VIEW")
        super.onDestroyView()
        _binding = null
        MediaPlayerManager.stopPlaying()
        mAdapter.clearContextualActionMode()
    }

}