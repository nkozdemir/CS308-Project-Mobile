package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

@Serializable
data class PlaylistSong(
    @SerialName("SongID") val songID: Int,
    @SerialName("Title") val title: String,
    @SerialName("ReleaseDate") val releaseDate: String,
    @SerialName("Album") val album: String,
    @SerialName("Length") val length: Int,
    @SerialName("SpotifyID") val spotifyID: String?,
    @SerialName("Image") val image: String?,
    @SerialName("Performers") val performers: List<Performer>,
    @SerialName("Genres") val genres: List<Genre>
)

@Serializable
data class PlaylistSongResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<PlaylistSong>
)

@Serializable
data class DeleteSongResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: Int?
)

class PlaylistsSongs : AppCompatActivity() {

    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView

    private lateinit var playlistSongsAdapter: PlaylistSongsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists_songs)

        val playlistName = intent.getStringExtra("playlistName")

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = "$playlistName"

        recyclerView = findViewById(R.id.playlistSongs)

        setupRecyclerView()

        val playlistID = intent.getIntExtra("playlistID", -1)

        if (playlistID != -1) {
            fetchSongsForPlaylist(playlistID)
        } else {
            Toast.makeText(this, "Invalid Playlist ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        playlistSongsAdapter = PlaylistSongsAdapter(emptyList(), this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistSongsAdapter
    }

    private fun fetchSongsForPlaylist(playlistID: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(kotlinx.serialization.json.Json)
                        }
                    }

                    client.post<PlaylistSongResponse> {
                        url("http://192.168.1.31:3000/playlist/getAllSongsForPlaylist")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("playlistID" to playlistID)
                    }
                }

                if (response.code == 200 && response.data != null) {
                    val playlistSongs: List<PlaylistSong> = response.data
                    playlistSongsAdapter.updateData(playlistSongs)
                } else {
                    println("Error: ${response.status}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlaylistsSongs, "Empty Playlist", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun onSongsToAddClick(view: View) {
        val playlistID = intent.getIntExtra("playlistID", -1)
        val intent = Intent(this,PlaylistSongsToAdd::class.java)
        intent.putExtra("playlistID", playlistID)
        startActivity(intent)
    }

    fun onDeleteClick(position: Int) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                val intent = Intent(this, PlaylistActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
