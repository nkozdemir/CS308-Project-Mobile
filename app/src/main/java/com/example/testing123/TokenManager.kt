package com.example.testing123

import android.content.Context
import android.content.SharedPreferences
private const val PREF_NAME = "TokenManagerPrefs"
private const val KEY_ACCESS_TOKEN = "accessToken"
private const val KEY_REFRESH_TOKEN = "refreshToken"
class TokenManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)


    companion object {
        private var instance: TokenManager? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = TokenManager(context)
            }
        }

        fun getInstance(): TokenManager {
            requireNotNull(instance) { "TokenManager must be initialized before use" }
            return instance!!
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }


    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        sharedPreferences.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_REFRESH_TOKEN).apply()
    }

}