package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class FriendInfo(
    @SerialName("UserID") val userID: Int,
    @SerialName("Name") val name: String,
    @SerialName("Email") val email: String,

)
@Serializable
data class Friends(
    @SerialName("FriendID") val friendID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("FriendUserID") val friendUserID: Int,
    @SerialName("FriendInfo") val friendInfo: FriendInfo,



    )

class FriendsActivity : AppCompatActivity() {

    private lateinit var editTextFriendEmail: EditText
    private lateinit var buttonAddFriend: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Initialize recyclerView before setting adapter and layout manager
        recyclerView = findViewById(R.id.recyclerView)

        friendsAdapter = FriendsAdapter(emptyList())  // Initialize with an empty list
        recyclerView.adapter = friendsAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)

        editTextFriendEmail = findViewById(R.id.editTextFriendEmail)
        buttonAddFriend = findViewById(R.id.addButton)

        buttonAddFriend.setOnClickListener {
            addFriend()
        }
        fetchAllFriends()
    }

    private fun addFriend() {
        val friendEmail = editTextFriendEmail.text.toString()

        if (friendEmail.isNotEmpty()) {
            mainScope.launch {
                try {
                    val accessToken = TokenManager.getInstance().getAccessToken()

                    // Make the API call to add a friend
                    val response: HttpResponse = withContext(Dispatchers.IO) {
                        val client = HttpClient {
                            install(JsonFeature) {
                                serializer = KotlinxSerializer(Json)
                            }
                        }

                        client.post("http://10.51.65.120:3000/friend/addFriend") {
                            header(HttpHeaders.Authorization, "Bearer $accessToken")
                            contentType(ContentType.Application.Json)
                            body = mapOf("friendEmail" to friendEmail)
                        }

                    }

                    Toast.makeText(this@FriendsActivity, "Add Successful", Toast.LENGTH_SHORT).show()


                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@FriendsActivity, "Add failed:${e.message}", Toast.LENGTH_SHORT).show()
                    // Handle exceptions
                }
            }
        } else {
            // Handle empty friendEmail case
            // Show a Toast or error message
            Toast.makeText(this, "Please enter your friend's e-mail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAllFriends(){
        val accessToken = TokenManager.getInstance().getAccessToken()

        mainScope.launch {
            try {
                val jsonString: String = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.get("http://10.51.65.120:3000/friend/getAllFriends") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }
                val apiResponse: ApiResponse<List<Friends>> = Json.decodeFromString(jsonString)

                if (apiResponse.status == "success") {
                    val friends : List<Friends> = apiResponse.data
                    friendsAdapter.updateData(friends)



                }

            }
            catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, HomeActivity::class.java)
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
