package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }

    fun allSongsClicked(view: View){
        val intent = Intent(this,AllAddedSongs::class.java)
        startActivity(intent)
    }
}