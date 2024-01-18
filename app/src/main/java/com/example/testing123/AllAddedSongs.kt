package com.example.testing123

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
@Serializable
data class ApiResponse<T>(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T
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
    //@SerialName("id") val id: String,
)


class AllAddedSongs : AppCompatActivity() {
    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var selectedSong: Song? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_added_songs)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerView)
        songAdapter = SongAdapter(
            onSongDeleteClick = { songId ->
                // Handle delete button click, make a post request to delete the song
                deleteSong(songId)
            },
            onSongRatingClick = { song ->
                // Handle rating button click, store the selected Song
                selectedSong = song
                onRatingClick()
            }
        )

        // Set up RecyclerView
        recyclerView.adapter = songAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@AllAddedSongs)

        fetchUserSongs()
    }
    private fun deleteSong(songId: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        val json = Json { ignoreUnknownKeys = true }

        mainScope.launch {
            try {
                val response: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(json)
                        }
                    }

                    client.post("http://192.168.1.31:3000/song/deleteSong/User") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("songId" to songId)
                    }
                }

                // Parse the JSON response
                val apiResponse: ApiResponse<Song> = json.decodeFromString(response)

                // Handle the parsed response
                if (apiResponse.status == "success") {
                    // The song was deleted successfully
                    val removedSong: Song? = apiResponse.data
                    removedSong?.let {
                        Log.d("DELETE_SUCCESS", "Song deleted successfully: $it")
                        // Refresh the song list
                        fetchUserSongs()
                    } ?: run {
                        Log.w("DELETE_SUCCESS", "Server response missing data for removed song.")
                    }
                } else {
                    // Handle error or show a message
                    Log.e("DELETE_ERROR", "Error deleting song. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("DELETE_ERROR", "Error deleting song.", e)
            }
        }
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

                    client.get("http://192.168.1.31:3000/song/getAllUserSongs") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }

                // Parse the JSON response
                val apiResponse: ApiResponse<List<Song>> = json.decodeFromString(jsonString)

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


    private fun onRatingClick() {
        selectedSong?.let {
            SongRepository.selectedSong = it
            startActivity(Intent(this, RatingActivity::class.java))
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

