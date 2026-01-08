package com.trios2025dej.activities

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.trios2025dej.R
import com.trios2025dej.models.Podcast

class PodcastDetailActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_detail)

        val podcast = intent.getSerializableExtra("podcast") as? Podcast
        if (podcast == null) {
            Toast.makeText(this, "Podcast data missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val titleView: TextView = findViewById(R.id.detailTitle)
        val imageView: ImageView = findViewById(R.id.detailImage)
        val playButton: Button = findViewById(R.id.detailPlayButton)

        titleView.text = podcast.title_original
        Glide.with(this)
            .load(podcast.image)
            .centerCrop()
            .into(imageView)

        playButton.setOnClickListener {
            podcast.audio?.let { url ->
                playAudio(url)
            } ?: Toast.makeText(this, "No audio available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAudio(url: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    Toast.makeText(this@PodcastDetailActivity, "Playback finished", Toast.LENGTH_SHORT).show()
                }
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@PodcastDetailActivity, "Error playing audio", Toast.LENGTH_SHORT).show()
                    true
                }
            } catch (e: Exception) {
                Toast.makeText(this@PodcastDetailActivity, "Cannot play this audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
