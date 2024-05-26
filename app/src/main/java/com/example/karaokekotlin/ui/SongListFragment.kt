package com.example.karaokekotlin.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.karaokekotlin.R
import com.example.karaokekotlin.adapter.SongAdapter
import com.example.karaokekotlin.databinding.FragmentSongListBinding
import com.example.karaokekotlin.util.Constants.Companion.DEFAULT_CONTENT
import com.example.karaokekotlin.util.NetworkListener
import com.example.karaokekotlin.util.NetworkResult
import com.example.karaokekotlin.viewmodel.MainViewModel
import com.example.karaokekotlin.viewmodel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SongListFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!

    private val mAdapter by lazy { SongAdapter() }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var songViewModel: SongViewModel
    private lateinit var networkListener: NetworkListener
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_song_list, container, false)
        binding.mainViewModel = mainViewModel
        binding.lifecycleOwner = this
        setupRecyclerview()
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)

                val search = menu.findItem(R.id.search)
                searchView = search.actionView as SearchView
                searchView?.isSubmitButtonEnabled = true
                searchView?.setOnQueryTextListener(this@SongListFragment)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkListener = NetworkListener()
                networkListener.checkNetworkAvailability(requireContext()).collect { status ->
                    Log.d("NetworkListener", status.toString())
                    mainViewModel.networkStatus = status
                    mainViewModel.showNetworkStatus()
                    requestApiData()
                }
            }
        }
        return binding.root
    }

    private fun setupRecyclerview() {
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestApiData() {
        Log.d("Song List Fragment", "Request API called")
        mainViewModel.getDefaultSongResponse(songViewModel.applyQueries())
        mainViewModel.defaultSongResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    Toast.makeText(requireContext(), response.message.toString(), Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }
            }
        }
    }

    private fun searchApiData(searchQuery: String) {
        showShimmerEffect()
        mainViewModel.getSearchSongResponse(songViewModel.applySearchQuery(searchQuery))
        mainViewModel.searchSongResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    val foodRecipe = response.data
                    foodRecipe?.let { mAdapter.setData(it) }
                }

                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }
            }
        }
    }
    private fun showShimmerEffect() {
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.visibility = View.VISIBLE
        binding.recyclerview.visibility = View.GONE
    }

    private fun hideShimmerEffect() {
        binding.shimmerFrameLayout.stopShimmer()
        binding.shimmerFrameLayout.visibility = View.GONE
        binding.recyclerview.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchApiData(query + DEFAULT_CONTENT)
            searchView.clearFocus()
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }


}