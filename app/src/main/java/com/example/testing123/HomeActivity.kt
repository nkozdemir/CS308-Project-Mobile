package com.example.testing123

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "HarmoniFuse"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.app_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signout) {
            runBlocking {
                val client = HttpClient(Android) {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                }

                try {

                    val accessToken = TokenManager.getInstance().getAccessToken()

                    if (accessToken != null) {

                        client.post<Unit>("http://192.168.1.31:3000/auth/logout") {
                            header("Authorization", "Bearer $accessToken")
                        }


                        TokenManager.getInstance().clearTokens()

                        Toast.makeText(this@HomeActivity, "Logout successful", Toast.LENGTH_SHORT).show()


                        val intent = Intent(this@HomeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {

                        Toast.makeText(this@HomeActivity, "Access token is null", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@HomeActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    client.close()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addSongsClicked(view: View){
        val intent = Intent(this,AddSongActivity::class.java)
        startActivity(intent)
    }

    fun dashboardClicked(view: View){
        val intent = Intent(this,DashboardActivity::class.java)
        startActivity(intent)
    }

    fun friendsClicked(view: View){
        val intent = Intent(this,FriendsActivity::class.java)
        startActivity(intent)
    }

    fun profileClicked(view: View){
        val intent = Intent(this,ProfileActivity::class.java)
        startActivity(intent)
    }

    fun recommendationsClicked(view: View){
        val intent = Intent(this,RecommendationAnalysis::class.java)
        startActivity(intent)
    }
}