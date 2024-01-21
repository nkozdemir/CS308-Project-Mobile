package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlaylistSongsAdapter(
    private var playlistSongs: List<PlaylistSong>,
    private val onDeleteClickListener: PlaylistsSongs
) : RecyclerView.Adapter<PlaylistSongsAdapter.PlaylistSongViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    class PlaylistSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
        val textViewAlbum: TextView = itemView.findViewById(R.id.textViewAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSongViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_song, parent, false)
        return PlaylistSongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistSongViewHolder, position: Int) {
        val currentPlaylistSong = playlistSongs[position]

        // Load image using Glide library
        Glide.with(holder.itemView.context)
            .load(currentPlaylistSong.image?.let { getImageUrl(it) })
            .into(holder.imageView)

        holder.textViewSongName.text = currentPlaylistSong.title
        holder.textViewPerformer.text = getPerformersText(currentPlaylistSong.performers)
        holder.textViewAlbum.text = currentPlaylistSong.album

        holder.itemView.findViewById<Button>(R.id.buttonDelete).setOnClickListener {
            onDeleteClickListener.onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int {
        return playlistSongs.size
    }

    private fun getPerformersText(performers: List<Performer>): String {
        // Concatenate performer names into a single string
        return performers.joinToString(", ") { it.name }
    }

    private fun getImageUrl(image: String): String {
        // Extracting the URL from the JSON string. You may need to implement parsing logic based on your data structure.
        // For simplicity, assuming the first URL in the JSON array is used.
        return image.split("\"url\":\"")[1].split("\"")[0]
    }
    fun updateData(newPlaylistSongs: List<PlaylistSong>) {
        playlistSongs = newPlaylistSongs
        notifyDataSetChanged()
    }
}