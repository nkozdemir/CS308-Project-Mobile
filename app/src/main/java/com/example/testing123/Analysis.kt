package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json



@Serializable
data class AnalysisResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<SongAnalysis>
)

@Serializable
data class TopRatedSongsRequest(
    @SerialName("decade") val decade: Int,
    @SerialName("count") val count: Int
)
@Serializable
data class AnalysisDailyAverageResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String?,
    @SerialName("data") val data: List<DailyAverageRating>
)
@Serializable
data class DailyAverageRating(
    @SerialName("date") val date: String,
    @SerialName("averageRating") val averageRating: Double
)


@Serializable
data class SongAnalysis(
    @SerialName("SongID") val songID: Int,
    @SerialName("Title") val title: String,
    @SerialName("ReleaseDate") val releaseDate: String,
    @SerialName("Album") val album: String,
    @SerialName("Length") val length: Int,
    @SerialName("SpotifyID") val spotifyID: String?,
    @SerialName("Image") val image: String?,
    @SerialName("Performers") val performers: List<Performer>
)

class Analysis : AppCompatActivity() {


    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        val decadeSpinner = findViewById<Spinner>(R.id.decadeSpinner2)
        val decades = listOf("1980s", "1990s", "2000s", "2010s","2020s") // Add more decades as needed
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, decades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        decadeSpinner.adapter = adapter
    }

    fun getTopRatedSongsByDecadeClicked(view: View) {
        val decadeSpinner = findViewById<Spinner>(R.id.decadeSpinner2)
        val amountOfSongsEditText = findViewById<EditText>(R.id.amountOfSongs2)

        val selectedDecade = getDecadeValue(decadeSpinner.selectedItem.toString())
        val numberOfSongsText = amountOfSongsEditText.text.toString()

        if (numberOfSongsText.isBlank()) {
            Toast.makeText(this@Analysis, "Please enter a valid number of songs", Toast.LENGTH_SHORT).show()
            return
        }

        val numberOfSongs = numberOfSongsText.toIntOrNull()

        if (numberOfSongs == null) {
            Toast.makeText(this@Analysis, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        fetchTopRatedSongsByDecade(selectedDecade, numberOfSongs)
    }

    private fun fetchTopRatedSongsByDecade(decade: Int, numberOfSongs: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json {
                                ignoreUnknownKeys = true
                            })
                        }
                    }

                    client.post<AnalysisResponse> {
                        url("http://192.168.1.31:3000/analysis/getTopRatedSongsByDecade")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("decade" to decade, "count" to numberOfSongs)
                    }
                }

                onResponseReceived(response)
            } catch (e: Exception) {
                Toast.makeText(this@Analysis, "Error fetching top-rated songs", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    fun yourTopRatedSongsByMonthClicked(view: View) {
        val numberOfMonthsText = findViewById<EditText>(R.id.month2).text.toString()

        val numberOfMonths = numberOfMonthsText.toInt()

        fetchTopRatedSongs(numberOfMonths)
    }

    private fun fetchTopRatedSongs(numberOfMonths: Int) {
        if (numberOfMonths <= 0) {
            Toast.makeText(this@Analysis, "Please enter a valid number of songs", Toast.LENGTH_SHORT).show()
            return
        }


        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json {
                                ignoreUnknownKeys = true
                            })
                        }
                    }


                    val responseBody = client.post<AnalysisResponse> {
                        url("http://192.168.1.31:3000/analysis/getTopRatedSongsFromLastMonths")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("month" to numberOfMonths)
                        println(body)
                    }


                    onResponseReceived(responseBody)
                }
            } catch (e: Exception) {

                Toast.makeText(this@Analysis, "Please Enter a Number", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }



    fun yourDailyAverageRatingClicked(view: View) {
        val numberOfDaysText = findViewById<EditText>(R.id.numberOfDays).text.toString()

        if (numberOfDaysText.isBlank()) {
            Toast.makeText(this@Analysis, "Please enter a valid number of days", Toast.LENGTH_SHORT).show()
            return
        }

        val numberOfDays = numberOfDaysText.toIntOrNull()

        if (numberOfDays == null) {
            Toast.makeText(this@Analysis, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            return
        }

        fetchDailyAverageRating(numberOfDays)
    }

    private fun fetchDailyAverageRating(numberOfDays: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()
        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json {
                                ignoreUnknownKeys = true
                            })
                        }
                    }

                    client.post<AnalysisDailyAverageResponse> {
                        url("http://192.168.1.31:3000/analysis/getDailyAverageRating")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = mapOf("day" to numberOfDays)
                    }
                }

                onDailyAverageRatingReceived(response)
            } catch (e: Exception) {
                Toast.makeText(this@Analysis, "Error fetching daily average rating", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun onDailyAverageRatingReceived(response: AnalysisDailyAverageResponse) {
        val textView2 = findViewById<TextView>(R.id.textView2)

        if (response.code == 200 && response.data != null && response.data.isNotEmpty()) {
            val averageRating = response.data.first().averageRating
            textView2.text = "Average Rating: $averageRating"
        } else {
            textView2.text = "Error: ${response.status}"
        }
    }





    private fun onResponseReceived(response: AnalysisResponse) {
        if (response.code == 200 && response.data != null) {

            launchNextActivity(response.data)
        } else {

            println("Error: ${response.status}")
        }
    }

    private fun launchNextActivity(analysis: List<SongAnalysis>){
        AnalysisDataHolder.analysis = analysis

        val intent = Intent(this@Analysis, DisplayAnalysis::class.java)
        startActivity(intent)
    }

    private fun getDecadeValue(selectedDecade: String): Int {
        // Implement logic to convert spinner value to decade integer (e.g., "1980s" to 1980)
        // You can modify this based on the actual values you want to send to the server
        return when (selectedDecade) {
            "1980s" -> 1980
            "1990s" -> 1990
            "2000s" -> 2000
            "2010s" -> 2010
            "2020s" -> 2020
            else -> 0 // Default value or handle other cases
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
