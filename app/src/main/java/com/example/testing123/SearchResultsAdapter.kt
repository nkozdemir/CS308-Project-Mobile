package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SearchResultsAdapter(
    private val searchDataList: List<DisplaySearchData>,
    private val onAddButtonClick: (Int, View) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val textViewSongName: TextView = itemView.findViewById(R.id.textViewSongName)
        val textViewPerformer: TextView = itemView.findViewById(R.id.textViewPerformer)
        val buttonAdd: Button = itemView.findViewById(R.id.buttonAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val searchData = searchDataList[position]

        holder.textViewSongName.text = searchData.songName
        holder.textViewPerformer.text = searchData.performerName

        Glide.with(holder.imageView.context)
            .load(searchData.imageUrl)
            .into(holder.imageView)

        holder.buttonAdd.setOnClickListener { view -> onAddButtonClick(position, view) }
    }




    override fun getItemCount(): Int {
        return searchDataList.size
    }
}
