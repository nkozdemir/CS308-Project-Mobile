package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SongRating(
    @SerialName("SongRatingID") val songRatingID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("SongID") val songID: Int,
    @SerialName("Rating") val rating: Int,
    @SerialName("Date") val date: String
)
@Serializable
data class SongRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: List<SongRating>
)
@Serializable
data class CreateSongRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: SongRating
)

@Serializable
data class SongRatingSubmitRequest(
    @SerialName("songId") val songID: Int,
    @SerialName("rating") val rating: Int
)
@Serializable
data class SongIdRequestBody(
    @SerialName("songId") val songID: Int
)
@Serializable
data class DeleteSongRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    //@SerialName("data") val data: List<SongRating>?
)





class RatingActivity : AppCompatActivity() {
    private lateinit var song: Song
    private val mainScope = MainScope()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rating)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)


        song = SongRepository.selectedSong!!
        val songNameTextView: TextView = findViewById(R.id.songName)

        val generalSongRating: TextView = findViewById(R.id.generalSongRating)

        val performerNameTextView: TextView = findViewById(R.id.performerName)
        val firstPerformerName = song.performers.firstOrNull()?.name ?: "Unknown Performer"

        performerNameTextView.text = "Performer: $firstPerformerName"

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_song -> {
                    // Handle song item click (if needed)
                    true
                }
                R.id.action_performer -> {
                    // Handle performer item click
                    val intent = Intent(this, PerformerRatingActivity::class.java)
                    startActivity(intent)

                    true
                }
                R.id.action_album -> {
                    // Handle album item click (if needed)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.menu.findItem(R.id.action_song)?.isChecked = true

        mainScope.launch {
            val averageRating = getGeneralSongRating(song)
            generalSongRating.text = "$averageRating"

        }

        songNameTextView.text = "Song: ${song.title}"
        // Check if the user has already rated the song
        lifecycleScope.launch {
            val userSongRating = getUserSongRating(song.songID)

            if (userSongRating != null) {
                displaySongRating(userSongRating)
            } else {
                // No rating entered at all
                displayNoRatings()
            }
        }
    }

    private fun displaySongRating(songRating: Int) {
        val userSongRatingTextView: TextView = findViewById(R.id.userSongRatingTextView)
        val submitSongRatingButton: Button = findViewById(R.id.submitRatingButton)
        val removeSongRatingButton: Button = findViewById(R.id.removeSongRatingButton)
        val ratingEditText: EditText = findViewById(R.id.ratingEditText)

        ratingEditText.visibility = View.INVISIBLE
        submitSongRatingButton.visibility = View.INVISIBLE

        userSongRatingTextView.text = "$songRating"

        removeSongRatingButton.setOnClickListener {
            // Remove the user's song rating from the server
            removeUserSongRating()
        }

    }


    private fun displayNoRatings() {
        val userSongRatingTextView: TextView = findViewById(R.id.userSongRatingTextView)
        val submitSongRatingButton: Button = findViewById(R.id.submitRatingButton)
        val removeSongRatingButton: Button = findViewById(R.id.removeSongRatingButton)
        val ratingEditText: EditText = findViewById(R.id.ratingEditText)

        userSongRatingTextView.visibility = View.INVISIBLE
        removeSongRatingButton.visibility = View.INVISIBLE

        submitSongRatingButton.setOnClickListener {
            val enteredRating = ratingEditText.text.toString().toIntOrNull()

            if (enteredRating != null) {
                // Call the submitSongRating function with the entered rating
                submitSongRating(enteredRating)
            } else {
                // Handle the case where the entered text is not a valid integer
                Toast.makeText(this@RatingActivity, "Please enter a valid rating", Toast.LENGTH_SHORT).show()
            }

    }
    }
    private suspend fun getUserSongRating(songId: Int): Int? {
        return getUserSongRatingFromApi(songId)
    }

    private fun submitSongRating(rating: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val url = "http://10.51.19.249:3000/rating/song/create"

                    val requestBody = SongRatingSubmitRequest(
                        songID = song.songID,
                        rating = rating
                    )

                    val jsonRequestBody = Json.encodeToString(requestBody)

                    client.post(url) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        body = TextContent(jsonRequestBody, ContentType.Application.Json)
                    }
                }

                val apiResponse: CreateSongRatingResponse = Json.decodeFromString(response)

                if (apiResponse.status == "success") {
                    Toast.makeText(this@RatingActivity, "Song rating submitted: $rating", Toast.LENGTH_SHORT).show()
                    recreate()

                    // You might want to update the UI or perform additional actions here
                } else {
                    Toast.makeText(this@RatingActivity, "Failed to submit song rating", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Error submitting song rating. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("NETWORK_ERROR", "Error submitting song rating.", e)
            }
        }
    }

    private fun removeUserSongRating() {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val url = "http://10.51.19.249:3000/rating/song/delete"

                    client.post<String>(url) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = SongIdRequestBody(song.songID)
                    }
                }

                val apiResponse: DeleteSongRatingResponse = Json.decodeFromString(response)

                if (apiResponse.status == "success") {
                    Toast.makeText(this@RatingActivity, "Song rating removed", Toast.LENGTH_SHORT).show()
                    recreate() // Recreate the activity to refresh the view
                } else {
                    Toast.makeText(this@RatingActivity, "Failed to remove song rating", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Error removing song rating. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("NETWORK_ERROR", "Error removing song rating.", e)
            }
        }
    }







    private suspend fun getUserSongRatingFromApi(songId: Int): Int? {
        val accessToken = TokenManager.getInstance().getAccessToken()

        try {
            val response: String = withContext(Dispatchers.IO) {
                val client = HttpClient {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(Json)
                    }
                }

                val url = "http://10.51.19.249:3000/rating/song/get/userid"

                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }

            val apiResponse: SongRatingResponse = Json.decodeFromString(response)

            if (apiResponse.status == "success") {
                val userRatings: List<SongRating> = apiResponse.data.filter { it.songID == songId }

                return userRatings.firstOrNull()?.rating
            } else {
                Log.e("API_ERROR", "Error fetching user song ratings. Status: ${apiResponse.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        // Handle exceptions
            Log.e("NETWORK_ERROR", "Error fetching user song ratings.", e)
        }

        return null
    }



     private suspend fun getGeneralSongRating(song: Song): Float {
        val accessToken = TokenManager.getInstance().getAccessToken()
         val requestBody = SongIdRequestBody(song.songID)

        try {
            val response: String = withContext(Dispatchers.IO) {
                val client = HttpClient {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(Json)
                    }
                }


                val url = "http://10.51.19.249:3000/rating/song/get/songid"



                client.post(url) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    body = requestBody
                }
            }


            val apiResponse: SongRatingResponse = Json.decodeFromString(response)

            if (apiResponse.status == "success") {
                val songRatings: List<SongRating> = apiResponse.data

                // Calculate the average rating, or perform any other logic based on your requirements
                val averageRating = calculateAverageRating(songRatings)
                println(averageRating)
                return averageRating


                //Update UI
            } else {
                // Handle error or show a message
                Log.e("API_ERROR", "Error fetching general song rating. Status: ${apiResponse.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions
            Log.e("NETWORK_ERROR", "Error fetching general song rating.", e)
        }
         return 0.0f
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










