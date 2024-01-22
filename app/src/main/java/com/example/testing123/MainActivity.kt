package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Serializable
data class LoginData(
    val email: String,
    val password: String,
)
class MainActivity : AppCompatActivity() {
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private val mainScope = MainScope()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameText = findViewById(R.id.UsernameText)
        passwordText = findViewById(R.id.PasswordText)



    }
    fun signInClicked(view: View) {
        val username = usernameText.text.toString()
        val password = passwordText.text.toString()
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        mainScope.launch(Dispatchers.IO) {
            val client = HttpClient(Android){
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
            try {
                val response: String = client.post {
                    url("http://192.168.1.31:3000/auth/login")
                    contentType(ContentType.Application.Json)
                    body = LoginData(
                        email = username,
                        password = password,
                    )
                }

                val jsonResponse = Json.decodeFromString<JsonElement>(response)
                val accessToken = jsonResponse.jsonObject["accessToken"]?.jsonPrimitive?.contentOrNull
                val refreshToken = jsonResponse.jsonObject["refreshToken"]?.jsonPrimitive?.contentOrNull

                // Save tokens using TokenManager
                if (accessToken != null && refreshToken != null) {
                    TokenManager.getInstance().saveTokens(accessToken, refreshToken)
                    println("Tokens saved successfully")
                } else {
                    println("Access token or refresh token is null")
                }

                //Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToHomeAct()
            }catch (e: Exception) {
                e.printStackTrace()

                println("Login failed: ${e.message}")

                //Toast.makeText(this@MainActivity, "Login failed", Toast.LENGTH_SHORT).show()
            } finally {
                client.close()
            }

        }
    }
    fun signUpClicked(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHomeAct() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}