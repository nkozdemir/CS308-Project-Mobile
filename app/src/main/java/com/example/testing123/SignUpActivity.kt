package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.post
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationData(
    val email: String,
    val password: String,
    val name: String
)


class SignUpActivity : AppCompatActivity() {
    private lateinit var emailText: EditText
    private lateinit var passwordText1: EditText
    private lateinit var passwordText2: EditText
    private lateinit var userText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailText = findViewById(R.id.emailText)
        userText = findViewById(R.id.userText)
        passwordText1 = findViewById(R.id.textPassword)
        passwordText2 =findViewById(R.id.textPassword2)
    }

    fun signUpClicked(view: View) {
        val username = userText.text.toString()
        val email = emailText.text.toString()
        val password1 = passwordText1.text.toString()
        val password2 = passwordText2.text.toString()
        if (username.isEmpty() || email.isEmpty() || password1.isEmpty()|| password2.isEmpty()) {
            Toast.makeText(this, "Enter username, e-mail and password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password1 != password2) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        runBlocking {
            val client = HttpClient(Android) {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }

        try {
            val response: String = client.post {
                url("http://10.51.56.188:3000/register")
                contentType(ContentType.Application.Json)
                body = RegistrationData(
                    email = email,
                    password = password1,
                    name = username
                )
            }
            Toast.makeText(this@SignUpActivity, "Registration successful", Toast.LENGTH_SHORT).show()
            navigateToHomeActivity()
        }
        catch (e: Exception) {
            e.printStackTrace()

            println("Response: ${e.message}")

            Toast.makeText(this@SignUpActivity, "Registration failed:${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            client.close()
        }

    }
    }
    fun onLoginClicked(view: View) {
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHomeActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }



    }
