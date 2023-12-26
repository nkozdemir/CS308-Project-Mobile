package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerformerRating(
    @SerialName("PerformerRatingID") val performerRatingID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("PerformerID") val performerID: Int,
    @SerialName("Rating") val rating: Int,
    @SerialName("Date") val date: String
)
@Serializable
data class PerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: List<PerformerRating>
)
@Serializable
data class CreatePerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: PerformerRating
)

@Serializable
data class PerformerRatingSubmitRequest(
    @SerialName("PerformerId") val performerID: Int,
    @SerialName("rating") val rating: Int
)
@Serializable
data class PerformerIdRequestBody(
    @SerialName("PerformerId") val performerID: Int
)
@Serializable
data class DeletePerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    //@SerialName("data") val data: List<SongRating>?
)

class PerformerRatingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performer_rating)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_song -> {
                    // Handle song item click (if needed)
                    val intent = Intent(this, RatingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    true
                }
                R.id.action_performer -> {
                    // Performer is already selected, do nothing
                    true
                }
                else -> false
            }
        }

        bottomNavigationView.menu.findItem(R.id.action_performer)?.isChecked = true
    }








    private fun calculateAverageRating(songRatings: List<SongRating>): Float {
        return if (songRatings.isNotEmpty()) {
            songRatings.map { it.rating }.average().toFloat()
        } else {
            0.0f // Default value when there are no ratings
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                val intent = Intent(this, AllAddedSongs::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)

                finish()
                return true
            }
            // Add other cases if needed
        }
        return super.onOptionsItemSelected(item)
    }
}