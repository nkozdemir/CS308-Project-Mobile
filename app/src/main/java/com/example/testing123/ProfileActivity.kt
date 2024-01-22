package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        supportActionBar?.title = ""

        userNameTextView = findViewById(R.id.UserName)
        emailTextView = findViewById(R.id.email)
        searchEditText = findViewById(R.id.editTextText3)
        searchButton = findViewById(R.id.searchButton)

        // Example usage of Get User Information endpoint
        getUserInformation()

        // Example usage of Search Users endpoint
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            searchUsers(query)
        }
    }

    private fun getUserInformation() {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = createHttpClient()
                    client.get<UserResponse> {
                        url("http://192.168.1.31:3000/user")
                        header("Authorization", "Bearer $accessToken")
                        contentType(ContentType.Application.Json)
                    }
                }

                if (response.code == 200 && response.data != null) {
                    val userData = response.data
                    userNameTextView.text = userData.name
                    emailTextView.text = userData.email
                } else {
                    println("Error: ${response.status}")
                    // Handle error, if needed
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error fetching user information", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun searchUsers(query: String) {
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val client = createHttpClient()
                    client.get<UsersResponse> {
                        url("http://192.168.1.31:3000/user/search?query=$query")
                        header("Authorization", "Bearer $accessToken")
                    }
                }

                if (response.code == 200 && response.data != null) {
                    val usersData = response.data
                    if (usersData.isNotEmpty()) {
                        val foundUser = usersData.first() // Assuming you want to display the first found user
                        val intent = Intent(this@ProfileActivity, DisplayUser::class.java).apply {
                            putExtra("userName", foundUser.name)
                            putExtra("userEmail", foundUser.email)
                        }
                        startActivity(intent)
                    } else {
                        // Handle the case where no users were found
                        Toast.makeText(this@ProfileActivity, "No users found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    println("Error: ${response.status}")
                    // Handle error, if needed
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Error searching users", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun createHttpClient(): HttpClient {
        return HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Serializable
    data class UserResponse(
        @SerialName("status") val status: String,
        @SerialName("code") val code: Int,
        @SerialName("message") val message: String,
        @SerialName("data") val data: UserData?
    )

    @Serializable
    data class UserData(
        @SerialName("UserID") val userID: Int,
        @SerialName("Name") val name: String,
        @SerialName("Email") val email: String
    )

    @Serializable
    data class UsersResponse(
        @SerialName("status") val status: String,
        @SerialName("code") val code: Int,
        @SerialName("message") val message: String,
        @SerialName("data") val data: List<UserData>?
    )
}
