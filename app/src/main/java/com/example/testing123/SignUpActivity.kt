package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class SignUpActivity : AppCompatActivity() {
    private lateinit var emailText: EditText
    private lateinit var passwordText1: EditText
    private lateinit var passwordText2: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailText = findViewById(R.id.emailText)
        passwordText1 = findViewById(R.id.textPassword)
        passwordText2 =findViewById(R.id.textPassword2)
    }

    fun signUpClicked(view: View) {
        val email = emailText.text.toString()
        val password1 = passwordText1.text.toString()
        val password2 = passwordText2.text.toString()
        if (email.isEmpty() || password1.isEmpty()|| password2.isEmpty()) {
            Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password1 != password2) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)

    }
    fun onLoginClicked(view: View) {
        navigateToMainActivity()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }



    }
