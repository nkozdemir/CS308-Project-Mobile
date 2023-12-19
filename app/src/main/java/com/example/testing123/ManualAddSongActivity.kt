package com.example.testing123

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
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
data class ManualAdd(
    //@SerialName("SongID") val songID: Int,
    @SerialName("title") val title: String,
    @SerialName("performers") val performers: String,
    @SerialName("album") val album: String,
    @SerialName("length") val length: String,
    @SerialName("genres") val genres: String,
@SerialName("releaseDate") val releaseDate: String
    // Add any other fields you need
)
class ManualAddSongActivity : AppCompatActivity() {

    private lateinit var songNameEditText: EditText
    private lateinit var performerNameEditText: EditText
    private lateinit var albumNameEditText: EditText
    private lateinit var songLengthEditText: EditText
    private lateinit var songGenreEditText: EditText
    private lateinit var releaseDateEditText: EditText

    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_add_song)

        songNameEditText = findViewById(R.id.editTextManualSongName)
        performerNameEditText = findViewById(R.id.editTextManualPerformerName)
        albumNameEditText = findViewById(R.id.editTextManualAlbumName)
        songLengthEditText = findViewById(R.id.editTextManualLenght)
        songGenreEditText = findViewById(R.id.editTextManualGenres)
        releaseDateEditText = findViewById(R.id.editTextManualReleaseDate)


    }

    fun addSongButtonClick(view: View) {
        val songName = songNameEditText.text.toString()
        val performerName = performerNameEditText.text.toString()
        val albumName = albumNameEditText.text.toString()
        val songLength = songLengthEditText.text.toString()
        val songGenre = songGenreEditText.text.toString()
        val releaseDate = releaseDateEditText.text.toString()


        if (songName.isEmpty() || performerName.isEmpty() || albumName.isEmpty() || songLength.isEmpty() || songGenre.isEmpty() || releaseDate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        addSong(songName, performerName, albumName, songLength, songGenre, releaseDate)
    }

    private fun addSong(
        songName: String,
        performerName: String,
        albumName: String,
        songLength: String,
        songGenre: String,
        releaseDate: String
    ) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                //val json = Json { ignoreUnknownKeys = true }

                val response: HttpResponse = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val requestBody = ManualAdd(
                        songName,
                        performerName,
                        albumName,
                        songLength,
                        songGenre,
                        releaseDate
                    )
                    println(requestBody)


                    client.post("http://10.51.65.120:3000/song/addCustomSong") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = requestBody
                    }
                }
                println("successful add")
            }catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }

        }
    }
}

