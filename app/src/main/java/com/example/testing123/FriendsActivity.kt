package com.example.testing123

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
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
import io.ktor.client.statement.readText
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

@Serializable
data class DeleteFriendResponse(
    @SerialName("status") val status: String,
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("data") val data: DeleteFriendData? = null
)

@Serializable
data class DeleteFriendData(
    @SerialName("FriendID") val friendID: Int,
    @SerialName("UserID") val userID: Int,
    @SerialName("FriendUserID") val friendUserID: Int
)


class FriendsActivity : AppCompatActivity(), FriendsItemClickListener {

    private lateinit var editTextFriendEmail: EditText
    private var friendsList: List<Friends> = emptyList()
    private lateinit var buttonAddFriend: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Initialize recyclerView before setting adapter and layout manager
        recyclerView = findViewById(R.id.friendSongs)

        friendsAdapter = FriendsAdapter(emptyList(), this)

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

    fun getAllFriendsSongs(view: View){

        val intent = Intent(this@FriendsActivity, FriendsSongsActivity::class.java)
        startActivity(intent)
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

                    client.get("http://192.168.1.31:3000/friend/getAllFriends") {
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

                        client.post("http://192.168.1.31:3000/friend/addFriend") {
                            header(HttpHeaders.Authorization, "Bearer $accessToken")
                            contentType(ContentType.Application.Json)
                            body = mapOf("friendEmail" to friendEmail)
                        }


                    }
                    fetchAllFriends()

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

    override fun onDeleteButtonClick(friend: Friends) {
        mainScope.launch {
            try {
                val accessToken = TokenManager.getInstance().getAccessToken()

                // Make the API call to delete a friend
                val response: HttpResponse = withContext(Dispatchers.IO) {
                    val client = HttpClient {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(Json)
                        }
                    }

                    client.post("http://192.168.1.31:3000/friend/deleteFriend") {
                        header(HttpHeaders.Authorization, "Bearer $accessToken")
                        contentType(ContentType.Application.Json)

                        body = mapOf("friendUserId" to friend.friendUserID)
                    }
                }

                val jsonString: String = response.readText()
                val apiResponse: DeleteFriendResponse = Json.decodeFromString(jsonString)

                if (apiResponse.status == "success") {
                    // Friend deleted successfully
                    Toast.makeText(this@FriendsActivity, "Friend deleted successfully", Toast.LENGTH_SHORT).show()



                    // Optionally, you can update the UI by removing the friend from the list
                    fetchAllFriends()
                } else {
                    // Handle the case when the deletion is not successful
                    Toast.makeText(this@FriendsActivity, "Failed to delete friend", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Toast.makeText(this@FriendsActivity, "Failed to delete friend: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
