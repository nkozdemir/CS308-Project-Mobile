package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AnalysisAdapter(private var analysisList: List<SongAnalysis>) : RecyclerView.Adapter<AnalysisAdapter.AnalysisViewHolder>() {

    class AnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalysisViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analysis, parent, false)
        return AnalysisViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AnalysisViewHolder, position: Int) {
        val currentAnalysis = analysisList[position]

        // Load image using Glide library

        holder.textViewSongName.text = currentAnalysis.title
        holder.textViewPerformer.text = getPerformersText(currentAnalysis.performers)
    }

    override fun getItemCount(): Int {
        return analysisList.size
    }

    private fun getPerformersText(performers: List<Performer>): String {
        // Concatenate performer names into a single string
        return performers.joinToString(", ") { it.name }
    }


    fun updateData(newAnalysisList: List<SongAnalysis>) {
        analysisList = newAnalysisList
        notifyDataSetChanged()
    }

    private fun getImageUrl(album: Album): String {
        // Use the first image URL from the "images" array in the Album
        return album.images.firstOrNull()?.url ?: "https://example.com/placeholder-image.jpg"
    }
}
