package com.trios2025dej.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trios2025dej.R
import com.trios2025dej.adapters.EpisodeListAdapter
import com.trios2025dej.models.Podcast
import com.trios2025dej.models.PodcastDetailResponse
import com.trios2025dej.network.ApiClient
import com.trios2025dej.network.PodcastApi
import com.trios2025dej.BuildConfig
import com.trios2025dej.utils.SubscriptionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PodcastDetailActivity : AppCompatActivity() {

    // MediaPlayer and state
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentAudioUrl: String? = null

    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var podcastImage: ImageView
    private lateinit var podcastTitle: TextView
    private lateinit var subscribeButton: Button
    private lateinit var playPauseButton: Button
    private lateinit var recyclerView: RecyclerView
    private var loadingProgress: ProgressBar? = null
    private var audioLoadingProgress: ProgressBar? = null

    private lateinit var podcast: Podcast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_detail)

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        podcastImage = findViewById(R.id.detailImage)
        podcastTitle = findViewById(R.id.detailTitle)
        subscribeButton = findViewById(R.id.subscribeButton)
        playPauseButton = findViewById(R.id.playPauseButton)
        recyclerView = findViewById(R.id.episodeRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        loadingProgress = findViewById(R.id.loadingProgress)
        audioLoadingProgress = findViewById(R.id.audioLoadingProgress)

        // Toolbar back button
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Podcast Details"
        }
        toolbar.setNavigationOnClickListener { finish() }

        // Get podcast from intent
        val data = intent.getSerializableExtra("podcast") as? Podcast
        if (data == null) {
            Toast.makeText(this, "Podcast data missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            podcast = data
        }

        // Display podcast info
        podcastTitle.text = podcast.title_original
        Glide.with(this)
            .load(podcast.image)
            .centerCrop()
            .into(podcastImage)

        // Subscribe / Unsubscribe button
        updateSubscribeButton()
        subscribeButton.setOnClickListener {
            if (SubscriptionManager.isSubscribed(this, podcast.id)) {
                SubscriptionManager.unsubscribe(this, podcast.id)
                Toast.makeText(this, "Unsubscribed", Toast.LENGTH_SHORT).show()
            } else {
                SubscriptionManager.subscribe(this, podcast) // pass full Podcast object
                Toast.makeText(this, "Subscribed", Toast.LENGTH_SHORT).show()
            }
            updateSubscribeButton()
        }



        // Play / Pause button
        playPauseButton.setOnClickListener { togglePlayPause() }

        // Restore state if exists
        savedInstanceState?.let { restorePlaybackState(it) }

        // Fetch episodes
        fetchEpisodes(podcast.id)
    }

    private fun updateSubscribeButton() {
        subscribeButton.text =
            if (SubscriptionManager.isSubscribed(this, podcast.id)) "Unsubscribe" else "Subscribe"
    }

    private fun fetchEpisodes(podcastId: String) {
        showLoading(true)
        val api = ApiClient.retrofit.create(PodcastApi::class.java)
        api.getPodcastDetails(BuildConfig.LISTEN_NOTES_API_KEY, podcastId)
            .enqueue(object : Callback<PodcastDetailResponse> {
                override fun onResponse(
                    call: Call<PodcastDetailResponse>,
                    response: Response<PodcastDetailResponse>
                ) {
                    showLoading(false)
                    val episodes = response.body()?.episodes ?: emptyList()
                    if (episodes.isEmpty()) {
                        Toast.makeText(
                            this@PodcastDetailActivity,
                            "No episodes found",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    recyclerView.adapter = EpisodeListAdapter(episodes) { episode ->
                        episode.audio?.let { url -> startNewAudio(url) }
                            ?: Toast.makeText(
                                this@PodcastDetailActivity,
                                "No audio for this episode",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }

                override fun onFailure(call: Call<PodcastDetailResponse>, t: Throwable) {
                    showLoading(false)
                    Toast.makeText(
                        this@PodcastDetailActivity,
                        "Failed to load episodes: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun startNewAudio(url: String) {
        try {
            if (currentAudioUrl == url) {
                togglePlayPause()
                return
            }

            releaseMediaPlayer()
            showAudioLoading(true)

            mediaPlayer = MediaPlayer().apply {
                setOnPreparedListener {
                    showAudioLoading(false)
                    start()
                    this@PodcastDetailActivity.isPlaying = true
                    playPauseButton.text = "Pause"
                    currentAudioUrl = url
                }
                setOnCompletionListener {
                    this@PodcastDetailActivity.isPlaying = false
                    playPauseButton.text = "Play"
                    currentAudioUrl = null
                }
                setOnErrorListener { _, _, _ ->
                    showAudioLoading(false)
                    this@PodcastDetailActivity.isPlaying = false
                    playPauseButton.text = "Play"
                    currentAudioUrl = null
                    Toast.makeText(
                        this@PodcastDetailActivity,
                        "Error playing audio",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }

                try {
                    setDataSource(url)
                    prepareAsync()
                } catch (e: Exception) {
                    showAudioLoading(false)
                    release()
                    mediaPlayer = null
                    Toast.makeText(
                        this@PodcastDetailActivity,
                        "Cannot play this audio",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unexpected error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun togglePlayPause() {
        mediaPlayer?.let { player ->
            try {
                if (isPlaying) {
                    player.pause()
                    isPlaying = false
                    playPauseButton.text = "Play"
                } else {
                    player.start()
                    isPlaying = true
                    playPauseButton.text = "Pause"
                }
            } catch (e: IllegalStateException) {
                Toast.makeText(
                    this,
                    "Cannot control playback right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: Toast.makeText(this, "No audio selected", Toast.LENGTH_SHORT).show()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
                reset()
                release()
            } catch (_: Exception) {
            }
        }
        mediaPlayer = null
        isPlaying = false
        currentAudioUrl = null
    }

    private fun showLoading(show: Boolean) {
        loadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showAudioLoading(show: Boolean) {
        audioLoadingProgress?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mediaPlayer?.let { player ->
            try {
                outState.putString("currentAudioUrl", currentAudioUrl)
                outState.putInt("currentPosition", player.currentPosition)
                outState.putBoolean("wasPlaying", isPlaying)
            } catch (_: Exception) {}
        }
    }

    private fun restorePlaybackState(savedInstanceState: Bundle) {
        val url = savedInstanceState.getString("currentAudioUrl")
        val pos = savedInstanceState.getInt("currentPosition", 0)
        val wasPlaying = savedInstanceState.getBoolean("wasPlaying", false)

        if (url != null && pos > 0) {
            currentAudioUrl = url
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener {
                        seekTo(pos)
                        if (wasPlaying) {
                            start()
                            this@PodcastDetailActivity.isPlaying = true
                            playPauseButton.text = "Pause"
                        }
                    }
                    setOnCompletionListener {
                        this@PodcastDetailActivity.isPlaying = false
                        playPauseButton.text = "Play"
                        currentAudioUrl = null
                    }
                } catch (_: Exception) {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
}
