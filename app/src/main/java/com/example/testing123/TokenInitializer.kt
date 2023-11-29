package com.example.testing123
import android.app.Application
class TokenInitializer : Application() {

    override fun onCreate() {
        super.onCreate()

        TokenManager.initialize(this)
    }
}