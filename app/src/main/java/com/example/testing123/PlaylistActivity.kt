package com.example.testing123

import PlaylistAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PlaylistDeleteResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: Int
)
@Serializable
data class Playlist(
    @SerialName("PlaylistID") val playlistID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("Name") val name: String,
    @SerialName("DateAdded") val dateAdded: String,
    @SerialName("Image") val image: String?
)

@Serializable
data class PlaylistResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<Playlist>
)
class PlaylistActivity : AppCompatActivity() {

    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = "Your Playlists"

        recyclerView = findViewById(R.id.playlists)


        setupRecyclerView()



        fetchPlaylists()
    }

    private fun setupRecyclerView() {
        playlistAdapter = PlaylistAdapter(emptyList(), onDeleteClickListener, onItemClickListener)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistAdapter
    }


    private fun fetchPlaylists() {

        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.get("http://192.168.1.31:3000/playlist/getAllUserPlaylists") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }

                val playlistResponse: PlaylistResponse = Json.decodeFromString(jsonString)

                if (playlistResponse.status == "success") {
                    val playlists: List<Playlist> = playlistResponse.data
                    playlistAdapter.updateData(playlists)
                } else {
                    Log.e("API_ERROR", "Error fetching user songs. Status: ${playlistResponse.status}")
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private val onDeleteClickListener: (Playlist) -> Unit = { playlist ->
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val requestBody = mapOf("playlistID" to playlist.playlistID)

                    client.post("http://192.168.1.31:3000/playlist/deletePlaylist") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = requestBody
                    }
                }

                val response: PlaylistDeleteResponse = Json.decodeFromString(jsonString)

                if (response.status == "success") {
                    // Playlist deleted successfully
                    // You may want to refresh your playlist data or update the UI accordingly
                    recreate()
                } else {
                    // Handle error case
                    Log.e("API_ERROR", "Error deleting playlist. Status: ${response.status}")
                }
            } catch (e: Exception) {
                // Handle exception
                Log.e("NETWORK_ERROR", "Error deleting playlist.", e)
            }
        }
    }



    private val onItemClickListener: (Playlist) -> Unit = { playlist ->
        // Handle item click
        val intent = Intent(this, PlaylistsSongs::class.java)
        intent.putExtra("playlistID", playlist.playlistID)
        intent.putExtra("playlistName", playlist.name)
        startActivity(intent)
    }

    fun createPlaylistClicked(view: View){
        val intent = Intent(this,AddPlaylist::class.java)
        startActivity(intent)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)

                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}