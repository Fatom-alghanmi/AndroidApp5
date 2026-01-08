package com.trios2025dej.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trios2025dej.R
import com.trios2025dej.models.Podcast

class PodcastAdapter(
    private val podcasts: List<Podcast>,
    private val onItemClick: (Podcast) -> Unit
) : RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    inner class PodcastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.podcastTitle)
        val image: ImageView = view.findViewById(R.id.podcastImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PodcastViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.podcast_item, parent, false))

    override fun getItemCount() = podcasts.size

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        val podcast = podcasts[position]
        holder.title.text = podcast.title_original
        Glide.with(holder.itemView.context)
            .load(podcast.image)
            .centerCrop()
            .into(holder.image)

        holder.itemView.setOnClickListener {
            onItemClick(podcast)
        }
    }
}
