package com.example.testing123

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.takeFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.SerialName
@Serializable
data class ApiResponse<T>(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: List<T>
)

@Serializable
data class Song(
    @SerialName("SongID") val songID: Int,
    @SerialName("Title") val title: String,
    @SerialName("ReleaseDate") val releaseDate: String,
    @SerialName("Album") val album: String,
    @SerialName("Length") val length: Int,
    @SerialName("SpotifyID") val spotifyID: String?,
    @SerialName("Image") val image: String?,
    @SerialName("Performers") val performers: List<Performer>
)

@Serializable
data class Performer(
    @SerialName("Name") val name: String,
)


class AllAddedSongs : AppCompatActivity() {
    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_added_songs)

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerView)
        songAdapter = SongAdapter()

        // Set up RecyclerView
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@AllAddedSongs)

        fetchUserSongs()
    }


    private fun fetchUserSongs() {
        val accessToken = TokenManager.getInstance().getAccessToken()
        val json = Json { ignoreUnknownKeys = true }  //Change this (add genres etc.)

        // GET request to retrieve user's songs
        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(json)
                        }
                    }

                    client.get("http://10.3.131.165:3000/song/getAllUserSongs") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }

                // Parse the JSON response
                val apiResponse: ApiResponse<Song> = json.decodeFromString(jsonString)

                if (apiResponse.status == "success") {
                    val songs: List<Song> = apiResponse.data

                    // Log the parsed data
                    Log.d("PARSED_DATA", songs.toString())

                    // Update the adapter with the new list of songs
                    songAdapter.setSongs(songs)
                } else {
                    // Handle error or show a message
                    Log.e("API_ERROR", "Error fetching user songs. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}

