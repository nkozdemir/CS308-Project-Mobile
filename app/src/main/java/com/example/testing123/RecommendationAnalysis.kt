package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Recommendation(
    @SerialName("SpotifyId") val spotifyId: String,
    @SerialName("Title") val title: String,
    @SerialName("Performer") val performers: List<Performers>,
    @SerialName("Album") val album: Album,
    @SerialName("Length") val length: Long
)
@Serializable
data class RecommendationResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<Recommendation>
)

class RecommendationAnalysis : AppCompatActivity() {

    private val httpClient = HttpClient()

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation_analysis)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

    }

    fun yourSongRatingsClicked(view: View) {
        val numberOfSongsText = findViewById<EditText>(R.id.number).text.toString()

        val numberOfSongs = if (numberOfSongsText.isNotEmpty()) {
            numberOfSongsText.toIntOrNull()
        } else {
            null
        }

        // Perform your song recommendation logic here
        fetchRecommendations(numberOfSongs)
    }

    fun yourLatestSongsClicked(view: View) {
        val numberOfSongsText = findViewById<EditText>(R.id.number).text.toString()

        val numberOfSongs = if (numberOfSongsText.isNotEmpty()) {
            numberOfSongsText.toIntOrNull()
        } else {
            null
        }

        // Perform your song recommendation logic here
        fetchLatestRecommendations(numberOfSongs)
    }

    fun yourFriendsRatingsClicked(view: View) {
        val numberOfSongsText = findViewById<EditText>(R.id.number).text.toString()

        val numberOfSongs = if (numberOfSongsText.isNotEmpty()) {
            numberOfSongsText.toIntOrNull()
        } else {
            null
        }

        // Perform your song recommendation logic here
        fetchFriendRatingRecommendations(numberOfSongs)
    }

    fun yourPerformerRatingsClicked(view: View) {
        val numberOfSongsText = findViewById<EditText>(R.id.number).text.toString()

        val numberOfSongs = if (numberOfSongsText.isNotEmpty()) {
            numberOfSongsText.toIntOrNull()
        } else {
            null
        }

        // Perform your song recommendation logic here
        fetchPerformerRatingRecommendations(numberOfSongs)
    }

    fun yourFriendLatestClicked(view: View) {
        val numberOfSongsText = findViewById<EditText>(R.id.number).text.toString()

        val numberOfSongs = if (numberOfSongsText.isNotEmpty()) {
            numberOfSongsText.toIntOrNull()
        } else {
            null
        }

        // Perform your song recommendation logic here
        fetchFriendLatestRecommendations(numberOfSongs)
    }

    private fun fetchFriendLatestRecommendations(numberOfSongs: Int?) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }


                    val responseBody = client.post<RecommendationResponse> {
                        url("http://192.168.1.31:3000/recommendation/friend/latest")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)

                        body = mapOf("numberOfSongs" to numberOfSongs)
                    }


                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationAnalysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()

                e.printStackTrace()
            }
        }
    }


    private fun fetchFriendRatingRecommendations(numberOfSongs: Int?) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }


                    val responseBody = client.post<RecommendationResponse> {
                        url("http://192.168.1.31:3000/recommendation/friend/rating")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)

                        body = mapOf("numberOfSongs" to numberOfSongs)
                    }


                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationAnalysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()

                e.printStackTrace()
            }
        }
    }




    private fun fetchPerformerRatingRecommendations(numberOfSongs: Int?) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    // Adjust the endpoint URL based on your API
                    val responseBody = client.post<RecommendationResponse> {
                        url("http://192.168.1.31:3000/recommendation/performer/rating")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        // Add the optional parameter to the request body
                        body = mapOf("numberOfSongs" to numberOfSongs)
                    }

                    // Process the response and launch the new activity
                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {
                // Handle exception
                Toast.makeText(this@RecommendationAnalysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }



    private fun fetchRecommendations(numberOfSongs: Int?) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    // Adjust the endpoint URL based on your API
                    val responseBody = client.post<RecommendationResponse> {
                        url("http://192.168.1.31:3000/recommendation/song/rating")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        // Add the optional parameter to the request body
                        body = mapOf("numberOfResults" to numberOfSongs)
                        println(body)
                    }

                    // Process the response and launch the new activity
                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {
                // Handle exception
                Toast.makeText(this@RecommendationAnalysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun fetchLatestRecommendations(numberOfSongs: Int?) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    // Adjust the endpoint URL based on your API
                    val responseBody = client.post<RecommendationResponse> {
                        url("http://192.168.1.31:3000/recommendation/song/latest")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        // Add the optional parameter to the request body
                        body = mapOf("numberOfResults" to numberOfSongs)
                    }

                    // Process the response and launch the new activity
                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {
                // Handle exception
                Toast.makeText(this@RecommendationAnalysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun onResponseReceived(response: RecommendationResponse) {
        if (response.code == 200 && response.data != null) {
            // Successfully received recommendations, launch the new activity
            launchNextActivity(response.data)
        } else {
            // Handle error response
            println("Error: ${response.status}")
        }
    }

    private fun launchNextActivity(recommendations: List<Recommendation>) {
        RecAnDataHolder.recommendations = recommendations

        val intent = Intent(this@RecommendationAnalysis, DisplayRecommendationsAnalysis::class.java)
        startActivity(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}