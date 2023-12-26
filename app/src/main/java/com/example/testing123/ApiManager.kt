package com.example.testing123

import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object ApiManager {
    private val mainScope = MainScope()
    suspend fun searchSongs(songName: String, performerName: String?, albumName: String?) {
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

                    val requestBody = SearchSongRequest(songName, performerName, albumName)
                    println(requestBody)


                    client.post("http://10.51.65.120:3000/spotifyapi/searchSong") {
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
                    //SearchDataHolder.displaySearchDataList = displaySearchDataList


                } else {
                    Log.e("API_ERROR", "Error searching songs. Status: ${searchResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }
        }
    }
}