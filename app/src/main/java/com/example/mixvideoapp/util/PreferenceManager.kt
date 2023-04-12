package com.example.mixvideoapp.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(
            Consts.PREFERENCE_NAME, Context.MODE_PRIVATE
        )
    }

    fun putLockApp(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getLockApp(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putPassCode(key: String, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getPassCode(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clearLockApp() {
        val editor = sharedPreferences.edit()
        putLockApp(Consts.PRE_KEY_LOCK, false)
        putPassCode(Consts.PRE_KEY_PASS, null)
        editor.apply()
    }
}