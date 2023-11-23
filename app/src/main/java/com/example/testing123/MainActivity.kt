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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val email: String,
    val password: String,
)
class MainActivity : AppCompatActivity() {
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
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
        runBlocking {
            val client = HttpClient(Android){
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
            try {
                val response: String = client.post {
                    url("http://10.51.65.120:3000/auth/login")
                    contentType(ContentType.Application.Json)
                    body = LoginData(
                        email = username,
                        password = password,
                    )
                }
                println("Response: $response")
                Toast.makeText(this@MainActivity, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToHomeAct()
            }catch (e: Exception) {
                e.printStackTrace()

                println("Login failed: ${e.message}")

                Toast.makeText(this@MainActivity, "Login failed:${e.message}", Toast.LENGTH_SHORT).show()
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