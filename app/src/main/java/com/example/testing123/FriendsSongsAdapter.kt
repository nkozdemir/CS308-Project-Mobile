package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testing123.Performer
import com.example.testing123.R

class FriendSongsAdapter(private var allFriendSongs: List<FriendSong>) : RecyclerView.Adapter<FriendSongsAdapter.FriendSongViewHolder>() {

    class FriendSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
        val textViewAlbum: TextView = itemView.findViewById(R.id.textViewAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendSongViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_song, parent, false)
        return FriendSongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendSongViewHolder, position: Int) {
        val currentFriendSong = allFriendSongs[position]

        // Load image using Glide library
        Glide.with(holder.itemView.context)
            .load(getImageUrl(currentFriendSong.image)) // Assuming "image" is a URL
            .into(holder.imageView)

        holder.textViewSongName.text = currentFriendSong.title
        holder.textViewPerformer.text = getPerformersText(currentFriendSong.performers)
        holder.textViewAlbum.text = currentFriendSong.album
    }

    override fun getItemCount(): Int {
        return allFriendSongs.size
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
    fun updateData(newFriendSongs: List<FriendSong>) {
        allFriendSongs = newFriendSongs
        notifyDataSetChanged()
    }
}
