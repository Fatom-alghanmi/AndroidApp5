package com.trios2025dej.activities

import android.content.Intent
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
    private lateinit var searchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.podcastRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchInput = findViewById(R.id.searchInput)

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
                override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                    val results = response.body()?.results?.filterNotNull() ?: emptyList()
                    if (results.isEmpty()) {
                        Toast.makeText(this@MainActivity, "No podcasts found", Toast.LENGTH_SHORT).show()
                    }

                    adapter = PodcastAdapter(results) { podcast ->
                        val intent = Intent(this@MainActivity, PodcastDetailActivity::class.java)
                        intent.putExtra("podcast", podcast)
                        startActivity(intent)
                    }
                    recyclerView.adapter = adapter
                }

                override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Error loading podcasts", Toast.LENGTH_SHORT).show()
                }
            })
    }
}