package ru.ifmo.se.app.util

import android.content.Context

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private const val TOKEN_KEY = "auth_token"

    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(TOKEN_KEY, token)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(TOKEN_KEY, null)
    }
}
