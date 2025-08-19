package com.example.comidasaborosa.util

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isInitialized(): Boolean = prefs != null

    fun saveUser(userId: Int, userName: String, userEmail: String) {
        prefs?.edit()?.apply {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            apply()
        }
    }

    fun getUserId(): Int = prefs?.getInt(KEY_USER_ID, -1) ?: -1
    fun getUserName(): String? = prefs?.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)

    fun clearUser() {
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean = isInitialized() && getUserId() != -1
}
