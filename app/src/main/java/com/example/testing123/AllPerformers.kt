package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class PerformerResponse<T>(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<AllPerformer>
)

@Serializable
data class AllPerformer(
    @SerialName("PerformerID") val performerID: Int,
    @SerialName("Name") val name: String,
    @SerialName("SpotifyID") val spotifyID: String?,
    @SerialName("Image") val image: String?,

)
class AllPerformers : AppCompatActivity() {

    private val mainScope = MainScope()
    private lateinit var recyclerView: RecyclerView
    private lateinit var performerAdapter: PerformerAdapter
    private var selectedPerformer: AllPerformer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_performers)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        recyclerView = findViewById(R.id.friendSongs)


        performerAdapter = PerformerAdapter(emptyList()) { performer ->
            // Handle rating button click, store the selected Song
            selectedPerformer = performer
            onRatingClick()
        }

        recyclerView.adapter = performerAdapter

        recyclerView.layoutManager = LinearLayoutManager(this@AllPerformers)



        fetchPerformers()


    }


    private fun fetchPerformers() {
        val accessToken = TokenManager.getInstance().getAccessToken()
        val json = Json { ignoreUnknownKeys = true }

        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(json)
                        }
                    }

                    client.get("http://192.168.1.31:3000/performer/getPerformer/user") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }

                val performerResponse: PerformerResponse<List<AllPerformer>> = json.decodeFromString(jsonString)

                if (performerResponse.status == "success") {
                    val performers: List<AllPerformer> = performerResponse.data

                    performerAdapter.updateData(performers)




                    Log.d("PARSED_DATA", performers.toString())


                } else {

                    Log.e("API_ERROR", "Error fetching user songs. Status: ${performerResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    private fun onRatingClick() {
        selectedPerformer?.let {
            SongRepository.selectedPerformer = it
            startActivity(Intent(this, PerformerRatingActivity::class.java))
        }
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