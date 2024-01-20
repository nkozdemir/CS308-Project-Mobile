package com.example.testing123

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.testing123.SongRepository.selectedPerformer
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.io.FileOutputStream


@Serializable
data class TextContentExport(val content: String)
@Serializable
data class PerformerRating(
    @SerialName("PerformerRatingID") val performerRatingID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("PerformerID") val performerID: Int,
    @SerialName("Rating") val rating: Int,
    @SerialName("Date") val date: String,
    @SerialName("PerformerInfo") val performerInfo: PerformerInfo
)

@Serializable
data class PerformerDate(
    @SerialName("fn") val fn: String,
    @SerialName("args") val args: List<String>
)

@Serializable
data class PerformerInfo(
    @SerialName("PerformerID") val performerID: Int,
    @SerialName("Name") val name: String,
    @SerialName("SpotifyID") val spotifyID: String,
    @SerialName("Image") val image: String?
)
@Serializable
data class PerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: List<PerformerRating>
)
@Serializable
data class CreatePerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: SubmitPerformerRating
)

@Serializable
data class SubmitPerformerRating(
    @SerialName("PerformerRatingID") val performerRatingID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("PerformerID") val performerID: Int,
    @SerialName("Rating") val rating: Int,
    @SerialName("Date") val date: PerformerDate

    )
@Serializable
data class PerformerIdRequestBody(
    @SerialName("PerformerId") val performerID: Int
)
@Serializable
data class DeletePerformerRatingResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: JsonElement
)

class PerformerRatingActivity : AppCompatActivity() {

    private val mainScope = MainScope()

    private lateinit var performer: AllPerformer

    private val EXPORT_FILE_NAME = "ratings_export.txt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performer_rating)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = ""

        val performerNameTextView: TextView = findViewById(R.id.performerName)

        val generalPerformerRating: TextView = findViewById(R.id.generalPerformerRating)

        selectedPerformer?.let {
            performer = it
            // Set the text of the TextView with the selected performer's name
            performerNameTextView.text = it.name
        }

        mainScope.launch {
            val averageRating = getGeneralPerformerRating(performer)
            generalPerformerRating.text = "$averageRating"

        }

        lifecycleScope.launch {
            val userPerformerRating = getUserPerformerRating(performer.performerID)

            if (userPerformerRating != null) {
                displayPerformerRating(userPerformerRating)
            } else {
                // No rating entered at all
                displayNoRatings()
            }
        }

        val exportButton: Button = findViewById(R.id.exportButton)
        exportButton.setOnClickListener {
            exportSongRatingsByPerformerName(performer.name)
        }

    }
    private fun displayPerformerRating(performerRating: Int) {
        val userPerformerRatingTextView: TextView = findViewById(R.id.userPerformerRatingTextView)
        val submitPerformerRatingButton: Button = findViewById(R.id.submitRatingButton)
        val removePerformerRatingButton: Button = findViewById(R.id.removeSongRatingButton)
        val ratingEditText: EditText = findViewById(R.id.ratingEditText)

        ratingEditText.visibility = View.INVISIBLE
        submitPerformerRatingButton.visibility = View.INVISIBLE

        userPerformerRatingTextView.text = "$performerRating"

        removePerformerRatingButton.setOnClickListener {
            // Remove the user's song rating from the server
            removeUserPerformerRating()
        }

    }

    private fun displayNoRatings() {
        val userPerformerRatingTextView: TextView = findViewById(R.id.userPerformerRatingTextView)
        val submitPerformerRatingButton: Button = findViewById(R.id.submitRatingButton)
        val removePerformerRatingButton: Button = findViewById(R.id.removeSongRatingButton)
        val ratingEditText: EditText = findViewById(R.id.ratingEditText)

        userPerformerRatingTextView.visibility = View.INVISIBLE
        removePerformerRatingButton.visibility = View.INVISIBLE

        submitPerformerRatingButton.setOnClickListener {
            val enteredRating = ratingEditText.text.toString().toIntOrNull()

            if (enteredRating != null) {
                // Call the submitSongRating function with the entered rating
                submitPerformerRating(enteredRating)
            } else {
                // Handle the case where the entered text is not a valid integer
                Toast.makeText(this@PerformerRatingActivity, "Please enter a valid rating", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private suspend fun getUserPerformerRating(performerId: Int): Int? {
        return getUserPerformerRatingFromApi(performerId)
    }

    private fun submitPerformerRating(rating: Int) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)

                        }
                    }

                    val url = "http://192.168.1.31:3000/rating/performer/create"



                    val requestBody = mapOf(
                        "performerId" to performer.performerID,
                        "rating" to rating
                    )

                    val jsonRequestBody = Json.encodeToString(requestBody)

                    client.post(url) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        body = TextContent(jsonRequestBody, ContentType.Application.Json)
                    }
                }

                val apiResponse: CreatePerformerRatingResponse = Json.decodeFromString(response)

                if (apiResponse.status == "success") {
                    Toast.makeText(this@PerformerRatingActivity, "Performer rating submitted: $rating", Toast.LENGTH_SHORT).show()
                    recreate()


                } else {
                    Toast.makeText(this@PerformerRatingActivity, "Failed to submit performer rating", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Error submitting song rating. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("NETWORK_ERROR", "Error submitting song rating.", e)
            }
        }
    }

    private fun removeUserPerformerRating() {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    val url = "http://192.168.1.31:3000/rating/performer/delete"
                    val requestBody = mapOf("performerId" to performer.performerID)

                    client.post(url) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = requestBody
                    }
                }

                val apiResponse: DeletePerformerRatingResponse = Json.decodeFromString(response)

                if (apiResponse.status == "success") {
                    Toast.makeText(this@PerformerRatingActivity, "Performer rating removed", Toast.LENGTH_SHORT).show()
                    recreate() // Recreate the activity to refresh the view
                } else {
                    Toast.makeText(this@PerformerRatingActivity, "Failed to remove Performer rating", Toast.LENGTH_SHORT).show()
                    Log.e("API_ERROR", "Error removing song rating. Status: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("NETWORK_ERROR", "Error removing song rating.", e)
            }
        }
    }




    private suspend fun getUserPerformerRatingFromApi(performerId: Int): Int? {
        val accessToken = TokenManager.getInstance().getAccessToken()

        try {
            val response: HttpResponse = withContext(Dispatchers.IO) {
                val client = HttpClient {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(Json)
                    }
                }

                val url = "http://192.168.1.31:3000/rating/performer/get/userid"

                client.get(url) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }

            val responseBody: String = response.readText()
            val apiResponse: PerformerRatingResponse = Json.decodeFromString(responseBody)

            if (apiResponse.status == "success") {
                val performerRatings: List<PerformerRating> = apiResponse.data.filter { it.performerID == performerId }

                return performerRatings.firstOrNull()?.rating
            } else {
                Log.e("API_ERROR", "Error fetching user's performer rating. Status: ${apiResponse.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions
            Log.e("NETWORK_ERROR", "Error fetching user's performer rating.", e)
        }

        return null
    }


    private suspend fun getGeneralPerformerRating(performer: AllPerformer): Double {
        val accessToken = TokenManager.getInstance().getAccessToken()

        try {
            val response: String = withContext(Dispatchers.IO) {
                val client = HttpClient {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(Json {
                            // Set coerceInputValues to true to handle null values
                            coerceInputValues = true
                        })
                    }
                }

                val url = "http://192.168.1.31:3000/rating/performer/get/performerid"

                val requestBody = mapOf("performerId" to performer.performerID)

                client.post(url) {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    body = requestBody
                    println(body)
                }
            }

            val apiResponse: PerformerRatingResponse = Json.decodeFromString(response)

            if (apiResponse.status == "success") {
                val performerRatings: List<PerformerRating> = apiResponse.data


                val averageRating = calculateAverageRating(performerRatings)
                println(averageRating)
                return averageRating


            } else {

                Log.e("API_ERROR", "Error fetching general song rating. Status: ${apiResponse.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exceptions
            Log.e("NETWORK_ERROR", "Error fetching general song rating.", e)
        }
        return 0.0
    }


    private fun calculateAverageRating(performerRatings: List<PerformerRating>): Double {
        return if (performerRatings.isNotEmpty()) {
            performerRatings.map { it.rating }.average().toDouble()
        } else {
            0.0
        }
    }



    private fun exportSongRatingsByPerformerName(performerName: String) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response: ByteArray = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(json = Json {
                                isLenient = true
                                ignoreUnknownKeys = true
                            })
                        }
                    }

                    val url = "http://192.168.1.31:3000/rating/song/export/performername"

                    val requestBody = mapOf("performerName" to performerName)

                    client.post(url) {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                        body = requestBody
                    }
                }


                saveRatingsToFile(response)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Log.e("NETWORK_ERROR", "Error exporting song ratings.", e)
            }
        }
    }

    private fun saveRatingsToFile(fileContent: ByteArray) {
        try {
            val fileName = "ratings_export.txt"
            val file = File(getExternalFilesDir(null), fileName)

            FileOutputStream(file).use { fileOutputStream ->
                fileOutputStream.write(fileContent)
            }

            // Notify the user that the export was successful
            Toast.makeText(this@PerformerRatingActivity, "Song ratings exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FILE_ERROR", "Error saving ratings to file.", e)

            // Notify the user about the error
            Toast.makeText(this@PerformerRatingActivity, "Error exporting song ratings", Toast.LENGTH_SHORT).show()
        }
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
            // Add other cases if needed
        }
        return super.onOptionsItemSelected(item)
    }
}