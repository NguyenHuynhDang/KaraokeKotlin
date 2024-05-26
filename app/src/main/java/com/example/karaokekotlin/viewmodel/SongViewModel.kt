package com.example.karaokekotlin.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.example.karaokekotlin.util.Constants.Companion.API_KEY
import com.example.karaokekotlin.util.Constants.Companion.DEFAULT_QUERY_PART
import com.example.karaokekotlin.util.Constants.Companion.DEFAULT_CONTENT
import com.example.karaokekotlin.util.Constants.Companion.DEFAULT_MAX
import com.example.karaokekotlin.util.Constants.Companion.DEFAULT_QUERY_TYPE
import com.example.karaokekotlin.util.Constants.Companion.QUERY_API_KEY
import com.example.karaokekotlin.util.Constants.Companion.QUERY_CONTENT
import com.example.karaokekotlin.util.Constants.Companion.QUERY_MAX_RESULT
import com.example.karaokekotlin.util.Constants.Companion.QUERY_PART
import com.example.karaokekotlin.util.Constants.Companion.QUERY_TYPE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    fun applyQueries(): HashMap<String, String> {
        val queries: HashMap<String, String> = HashMap()
        queries[QUERY_API_KEY] = API_KEY
        queries[QUERY_PART] = DEFAULT_QUERY_PART
        queries[QUERY_CONTENT] = DEFAULT_CONTENT
        queries[QUERY_MAX_RESULT] = DEFAULT_MAX
        queries[QUERY_TYPE] = DEFAULT_QUERY_TYPE
        return queries
    }

    fun applySearchQuery(searchQuery: String): HashMap<String, String> {
        val queries: HashMap<String, String> = HashMap()
        queries[QUERY_API_KEY] = API_KEY
        queries[QUERY_PART] = DEFAULT_QUERY_PART
        queries[QUERY_CONTENT] = searchQuery
        queries[QUERY_MAX_RESULT] = DEFAULT_MAX
        queries[QUERY_TYPE] = DEFAULT_QUERY_TYPE

        return queries
    }
}