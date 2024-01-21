package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PlaylistSongsToAdd : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistSongsAddAdapter: PlaylistSongsAddAdapter
    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs_to_add)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = ""

        recyclerView = findViewById(R.id.playlistSongsToAdd)
        setupRecyclerView()
        val playlistID = intent.getIntExtra("playlistID", -1)

        if (playlistID != -1) {
            fetchSongsToAdd(playlistID)
            Toast.makeText(this, "Songs Found", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Invalid Playlist ID", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun setupRecyclerView() {
        playlistSongsAddAdapter = PlaylistSongsAddAdapter(emptyList(), this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = playlistSongsAddAdapter
    }

    private fun fetchSongsToAdd(playlistID: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.post<PlaylistSongResponse> {
                        url("http://192.168.1.31:3000/playlist/getSongsToAdd")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("playlistID" to playlistID)
                    }
                }

                if (response.code == 200 && response.data != null) {
                    val songsToAdd: List<PlaylistSong> = response.data
                    playlistSongsAddAdapter.updateData(songsToAdd)
                } else {
                    println("Error: ${response.status}")
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlaylistSongsToAdd, "Error fetching Songs to Add", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun onAddClick(position: Int) {
        val playlistID = intent.getIntExtra("playlistID", -1)
        val selectedSong = playlistSongsAddAdapter.songsToAdd[position]



        if (selectedSong != null) {
            addSong(position,playlistID)
        } else {
            println("Invalid position or song data")
        }
    }
    private fun addSong(position: Int, playlistID: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        val selectedSong = playlistSongsAddAdapter.songsToAdd[position]

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.post<PlaylistSongResponse> {
                        url("http://192.168.1.31:3000/playlist/addSongsToPlaylist")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf(
                            "playlistID" to playlistID,
                            "songIDs" to listOf(selectedSong.songID)
                        )
                    }
                }

                if (response.code == 200 && response.data != null) {
                    println("Song added to playlist successfully")
                    // Handle success, if needed
                } else {
                    println("Error: ${response.status}")
                    // Handle error, if needed
                }
            } catch (e: Exception) {
                println("Error adding song to playlist: ${e.message}")
                // Handle exception, if needed
            }
        }
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