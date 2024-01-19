package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testing123.ApiResponse
import com.example.testing123.Performer
import com.example.testing123.Performers
import com.example.testing123.R
import com.example.testing123.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FriendSong(
    @SerialName("SongID") val songID: Int,
    @SerialName("Title") val title: String,
    @SerialName("ReleaseDate") val releaseDate: String,
    @SerialName("Album") val album: String,
    @SerialName("Length") val length: Int,
    @SerialName("SpotifyID") val spotifyID: String,
    @SerialName("Image") val image: String,
    @SerialName("Performers") val performers: List<Performer>,
    @SerialName("Genres") val genres: List<Genre>
)

@Serializable
data class Genre(
    @SerialName("Name") val name: String
)

class FriendsSongsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songsAdapter: FriendSongsAdapter
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_songs)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        recyclerView = findViewById(R.id.friendSongs)
        songsAdapter = FriendSongsAdapter(emptyList()) // Create an adapter for the RecyclerView
        recyclerView.adapter = songsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch friend songs
        fetchFriendSongs()
    }

    private fun fetchFriendSongs() {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.get("http://192.168.1.31:3000/friend/getAllFriendSongs") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }

                val apiResponse: ApiResponse<List<FriendSong>> = Json.decodeFromString(jsonString)

                if (apiResponse.status == "success") {
                    val friendSongs: List<FriendSong> = apiResponse.data
                    songsAdapter.updateData(friendSongs)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                val intent = Intent(this, FriendsActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }
}
