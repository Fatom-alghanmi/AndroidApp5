package com.trios2025dej.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trios2025dej.R
import com.trios2025dej.adapters.PodcastAdapter
import com.trios2025dej.models.Podcast
import com.trios2025dej.models.SearchResponse
import com.trios2025dej.network.ApiClient
import com.trios2025dej.network.PodcastApi
import com.trios2025dej.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PodcastAdapter
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.podcastRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchInput = findViewById(R.id.searchInput)

        // Search-as-you-type
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchPodcasts(query)
                } else {
                    adapter = PodcastAdapter(emptyList()) {}
                    recyclerView.adapter = adapter
                }
            }
        })
    }

    private fun searchPodcasts(query: String) {
        val api = ApiClient.retrofit.create(PodcastApi::class.java)
        api.searchPodcasts(BuildConfig.LISTEN_NOTES_API_KEY, query)
            .enqueue(object : Callback<SearchResponse> {
                override fun onResponse(
                    call: Call<SearchResponse>,
                    response: Response<SearchResponse>
                ) {
                    val results = response.body()?.results ?: emptyList()
                    if (results.isEmpty()) {
                        Toast.makeText(this@MainActivity, "No podcasts found", Toast.LENGTH_SHORT)
                            .show()
                    }

                    // Create adapter with Play functionality
                    adapter = PodcastAdapter(results) { podcast ->
                        playPodcast(podcast.audio) // audio URL
                    }
                    recyclerView.adapter = adapter
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error loading podcasts", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun playPodcast(url: String) {
        // Stop previous playback if any
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    Toast.makeText(this@MainActivity, "Playback finished", Toast.LENGTH_SHORT)
                        .show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@MainActivity, "Error playing audio", Toast.LENGTH_SHORT)
                        .show()
                    true
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Cannot play this audio", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}