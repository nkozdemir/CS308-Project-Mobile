package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchSongResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<SearchResult>?
)
@Serializable
data class SearchSongRequest(
    @SerialName("trackName") val trackName: String,
    @SerialName("performerName") val performerName: String?,
    @SerialName("albumName") val albumName: String?
)
@Serializable
data class Performers(
    @SerialName("name") val name: String,
    @SerialName("id") val id: String
)
@Serializable
data class Image(
    @SerialName("height") val height: Int,
    @SerialName("width") val width: Int,
    @SerialName("url") val url: String
)
@Serializable
data class Album(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("release_date") val releaseDate: String,
    @SerialName("images") val images: List<Image>
)
@Serializable
data class SearchResult(
    //@SerialName("SongID") val songID: Int,
    @SerialName("Title") val title: String,
    //@SerialName("ReleaseDate") val releaseDate: String,
    @SerialName("Album") val album: Album,
    @SerialName("Length") val length: Int,
    @SerialName("Genres") val genres: List<String>,
    @SerialName("SpotifyId") val spotifyID: String,
    @SerialName("Performer") val performers: List<Performers>
    // Add any other fields you need
)
@Serializable
data class DisplaySearchData(
    val songName: String,
    val performerName: String,
    val imageUrl: String,
    val spotifyID: String
)


class AddSongActivity : AppCompatActivity() {

    private lateinit var songNameEditText :EditText
    private lateinit var performerNameEditText :EditText
    private lateinit var albumNameEditText :EditText
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_song)
        songNameEditText = findViewById(R.id.editTextSongName)
        performerNameEditText = findViewById(R.id.editTextPerformerName)
        albumNameEditText = findViewById(R.id.editTextAlbumName)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
    }

    fun searchButtonClick(view: View) {
        val songName = songNameEditText.text.toString()
        val performerName = performerNameEditText.text.toString()
        val albumName = albumNameEditText.text.toString()

        // Call your backend API to search for songs
        searchSongs(songName, performerName, albumName)
    }
    fun csvButtonClick(view: View) {
        val intent = Intent(this,CsvUploadActivity::class.java)
        startActivity(intent)
    }

    fun ManualAddButtonClick(view: View) {
        val intent = Intent(this,ManualAddSongActivity::class.java)
        startActivity(intent)
    }

    private fun searchSongs(songName: String, performerName: String, albumName: String) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        if (songName.isEmpty()) {
            Toast.makeText(this, "Please enter the song name", Toast.LENGTH_SHORT).show()
            return
        }
        mainScope.launch {
            try {
                //val json = Json { ignoreUnknownKeys = true }

                val response: HttpResponse = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val requestBody = SearchSongRequest(songName, performerName, albumName)
                    println(requestBody)


                    client.post("http://192.168.1.31:3000/spotifyapi/searchSong") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = requestBody
                    }
                }

                // Get the response content
                val responseContent: String = response.readText()
                println(responseContent)

                // Parse the JSON response
                val searchResponse: SearchSongResponse = Json.decodeFromString(responseContent)

                if (searchResponse.status == "success") {
                    val searchResults: List<SearchResult> = searchResponse.data ?: emptyList()

                    val displaySearchDataList = searchResults.map { result ->
                        val songName = result.title
                        val performerName = result.performers.firstOrNull()?.name ?: ""
                        val imageUrl = result.album.images.firstOrNull()?.url ?: ""
                        val spotifyID = result.spotifyID


                        DisplaySearchData(songName, performerName, imageUrl, spotifyID)
                    }
                    SearchDataHolder.displaySearchDataList = displaySearchDataList

                    val intent = Intent(this@AddSongActivity, DisplaySearchActivity::class.java)
                    startActivity(intent)

                } else {
                    Log.e("API_ERROR", "Error searching songs. Status: ${searchResponse.status}")
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
