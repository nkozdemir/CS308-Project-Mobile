package com.example.testing123

// PerformerAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testing123.AllPerformer
import com.example.testing123.R

class PerformerAdapter(
    private var performers: List<AllPerformer>,
    private val onRateClickListener: (AllPerformer) -> Unit
) : RecyclerView.Adapter<PerformerAdapter.PerformerViewHolder>() {

    class PerformerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textViewPerformer)
        val btnRate: Button = itemView.findViewById(R.id.buttonRating)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPerformer)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerformerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_performer, parent, false)
        return PerformerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PerformerViewHolder, position: Int) {
        val performer = performers[position]
        holder.textName.text = performer.name

        Glide.with(holder.itemView.context)
            .load(performer.image?.let { getImageUrl(it) }) // Assuming "image" is a URL
            .into(holder.imageView)


        holder.btnRate.setOnClickListener { onRateClickListener(performer) }

        // Bind other data to TextViews or ImageViews
    }

    private fun getImageUrl(image: String): String {
        // Extracting the URL from the JSON string. You may need to implement parsing logic based on your data structure.
        // For simplicity, assuming the first URL in the JSON array is used.
        return image.split("\"url\":\"")[1].split("\"")[0]
    }

    override fun getItemCount(): Int {
        return performers.size
    }

    fun updateData(performerList: List<AllPerformer>) {
        performers = performerList
        notifyDataSetChanged()
    }
}
