package com.example.testing123
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testing123.R

class RecommendationAdapter(
    private val recommendations: List<Recommendation>,
    private val onItemClickListener: (Int) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation_analysis, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = recommendations[position]

        // Bind data to UI components
        val imageUrl = recommendation.album.images.firstOrNull()?.url
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.textViewSongName.text = recommendation.title
        holder.textViewPerformer.text = recommendation.performers.firstOrNull()?.name ?: ""

    }

    override fun getItemCount(): Int {
        return recommendations.size
    }
}
