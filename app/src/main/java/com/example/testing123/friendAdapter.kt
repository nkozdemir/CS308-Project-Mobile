package com.example.testing123

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendsAdapter(private var friendsList: List<Friends>) :
    RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName: TextView = itemView.findViewById(R.id.friendName)
        val performerTextView: TextView = itemView.findViewById(R.id.performerTextView)
        val friendSongsButton: Button = itemView.findViewById(R.id.friendSongsButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val currentFriend = friendsList[position]

        holder.friendName.text = "Name: ${currentFriend.friendInfo.name}"
        holder.performerTextView.text = "Mail: ${currentFriend.friendInfo.email}"

        holder.friendSongsButton.setOnClickListener {
            // Handle friend songs button click
        }

        holder.deleteButton.setOnClickListener {
            // Handle delete button click
        }
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateData(newFriendsList: List<Friends>) {
        friendsList = newFriendsList
        notifyDataSetChanged()
    }

}
