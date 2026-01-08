package com.trios2025dej.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trios2025dej.R
import com.trios2025dej.models.Episode

class EpisodeListAdapter(
    private val episodes: List<Episode>,
    private val onPlayClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeListAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.episodeTitle)
        val image: ImageView = view.findViewById(R.id.episodeImage)
        val playBtn: Button = view.findViewById(R.id.playEpisodeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.episode_item, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun getItemCount() = episodes.size

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        holder.title.text = episode.title

        Glide.with(holder.itemView.context)
            .load(episode.image)
            .into(holder.image)

        holder.playBtn.setOnClickListener {
            onPlayClick(episode)
        }
    }
}
