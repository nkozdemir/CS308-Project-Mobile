package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlaylistSongsAddAdapter(
    var songsToAdd: List<PlaylistSong>,
    private val onAddClickListener: PlaylistSongsToAdd
) : RecyclerView.Adapter<PlaylistSongsAddAdapter.SongToAddViewHolder>() {

    interface OnAddClickListener {
        fun onAddClick(position: Int)
    }

    class SongToAddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
        val textViewAlbum: TextView = itemView.findViewById(R.id.textViewAlbum)
        val buttonAdd: Button = itemView.findViewById(R.id.buttonAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongToAddViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_song_to_add, parent, false)
        return SongToAddViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongToAddViewHolder, position: Int) {
        val currentSongToAdd = songsToAdd[position]

        // Load image using Glide library
        Glide.with(holder.itemView.context)
            .load(currentSongToAdd.image?.let { getImageUrl(it) })
            .into(holder.imageView)

        holder.textViewSongName.text = currentSongToAdd.title
        holder.textViewPerformer.text = getPerformersText(currentSongToAdd.performers)
        holder.textViewAlbum.text = currentSongToAdd.album

        holder.buttonAdd.setOnClickListener {
            // Pass only position to the click listener
            onAddClickListener.onAddClick(position)
        }
    }

    override fun getItemCount(): Int {
        return songsToAdd.size
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

    fun updateData(newSongsToAdd: List<PlaylistSong>) {
        songsToAdd = newSongsToAdd
        notifyDataSetChanged()
    }
}
