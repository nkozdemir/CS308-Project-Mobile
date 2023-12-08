package com.example.testing123

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.example.testing123.SearchDataHolder.displaySearchDataList
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DisplaySearchActivity : AppCompatActivity() {
    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_search)

        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val adapter = SearchResultsAdapter(SearchDataHolder.displaySearchDataList ?: emptyList()) { position, view ->
            // Handle add button click here
            // You can use the position to identify the clicked item
            // For example, SearchDataHolder.displaySearchDataList?.get(it) to get the clicked item
            // Add your logic to handle the add button click
            onAddButtonClick(position, view)
        }
        recyclerView.adapter = adapter
    }

    fun onAddButtonClick(position: Int, view: View) {
        val displaySearchData = SearchDataHolder.displaySearchDataList?.get(position)

        displaySearchData?.let {
            val context = view.context
            MainScope().launch {
                try {
                    val accessToken = TokenManager.getInstance().getAccessToken()
                    val spotifyId = it.spotifyID

                    if (accessToken != null) {
                        addSpotifySong(context, accessToken, spotifyId)
                    } else {
                        Log.e("API_ERROR", "Access token is null.")
                    }
                } catch (e: Exception) {
                    Log.e("API_ERROR", "Error adding Spotify song.", e)
                }
            }
        }
    }



    private suspend fun addSpotifySong(context: Context, accessToken: String, spotifyId: String) {
        val client = HttpClient {
            install(JsonFeature)
        }

        try {
            val response: String = client.post("http://10.59.5.69:3000/song/addSpotifySong") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
                body = mapOf("spotifyId" to spotifyId)
            }

            // Check if the response indicates success
            if (response.contains("success", ignoreCase = true)) {
                showToast(context, "Song Added")
            } else {
                Log.e("API_ERROR", "Error adding Spotify song. Response: $response")
            }

        } catch (e: Exception) {
            Log.e("API_ERROR", "Error adding Spotify song.", e)
        } finally {
            client.close()
        }
    }
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

