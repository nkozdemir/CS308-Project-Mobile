package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
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
data class CreatePlaylistRequest(
    @SerialName("playlistName") val playlistName: String,
    @SerialName("songIDs") val songIDs: List<Int> = emptyList()
)
@Serializable
data class CreatePlaylistResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: Playlist?
)

class AddPlaylist : AppCompatActivity() {

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_playlist)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        supportActionBar?.title = ""
    }

    fun createButtonClicked(view: View) {
        val playlistNameEditText: EditText = findViewById(R.id.editTextText)
        val songIdsEditText: EditText = findViewById(R.id.editTextText2)

        val playlistName = playlistNameEditText.text.toString().trim()
        val songIds = songIdsEditText.text.toString().trim()

        if (playlistName.isEmpty()) {
            // Playlist name is required
            Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        mainScope.launch {
            try {
                val response: CreatePlaylistResponse = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                        defaultRequest {
                            contentType(ContentType.Application.Json)
                        }
                    }

                    val accessToken = TokenManager.getInstance().getAccessToken()

                    client.post<CreatePlaylistResponse>("http://192.168.1.31:3000/playlist/createPlaylist") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")

                        body = CreatePlaylistRequest(playlistName)
                    }
                }

                if (response.status == "success") {
                    val createdPlaylist = response.data
                    Toast.makeText(this@AddPlaylist, "Playlist created successfully", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@AddPlaylist, "Failed to create playlist. ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle exception
                e.printStackTrace()
            }
        }
        val intent = Intent(this,PlaylistActivity::class.java)
        startActivity(intent)
    }

    private fun parseSongIds(songIds: String): List<Int> {
        // Implement logic to parse the songIds string into a List<Int>
        // For example, you can split the string and convert each part to an Int
        return songIds.split(",").mapNotNull { it.toIntOrNull() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                val intent = Intent(this, AllPerformers::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)

                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}