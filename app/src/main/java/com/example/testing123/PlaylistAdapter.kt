import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.testing123.Playlist
import com.example.testing123.R

class PlaylistAdapter(
    private var playlists: List<Playlist>,
    private val onDeleteClickListener: (Playlist) -> Unit,
    private val onItemClickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.textViewPlaylist)
        val btnRate: Button = itemView.findViewById(R.id.buttonDelete)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPlaylist)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.textName.text = playlist.name

        Glide.with(holder.itemView.context)
            .load(playlist.image?.let { getImageUrl(it) }) // Assuming "image" is a URL
            .into(holder.imageView)


        holder.btnRate.setOnClickListener { onDeleteClickListener(playlist) }

        holder.textName.setOnClickListener { onItemClickListener(playlist) }

        // Bind other data to TextViews or ImageViews
    }

    private fun getImageUrl(image: String): String {
        // Extracting the URL from the JSON string. You may need to implement parsing logic based on your data structure.
        // For simplicity, assuming the first URL in the JSON array is used.
        return image.split("\"url\":\"")[1].split("\"")[0]
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    fun updateData(playlistList: List<Playlist>) {
        playlists = playlistList
        notifyDataSetChanged()
    }
}