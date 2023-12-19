package com.example.testing123

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testing123.R



class SongAdapter(
    private val onSongDeleteClick: (Int) -> Unit,
    private val onSongRatingClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var songs: List<Song> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)

        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)

        holder.deleteButton.setOnClickListener {
            onSongDeleteClick(song.songID)
        }

        holder.ratingButton.setOnClickListener {
            onSongRatingClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun setSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val performerTextView: TextView = itemView.findViewById(R.id.performerTextView)
        private val albumTextView: TextView = itemView.findViewById(R.id.albumTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val ratingButton: Button = itemView.findViewById(R.id.ratingButton)

        fun bind(song: Song) {
            titleTextView.text = "Title: ${song.title}"
            performerTextView.text = "Performer: ${song.performers.joinToString { it.name }}"
            albumTextView.text = "Album: ${song.album}"
        }
    }


}



