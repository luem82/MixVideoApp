package com.example.mixvideoapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.PreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(this)
        val isLock = preferenceManager.getLockApp(Consts.PRE_KEY_LOCK)

        Handler(Looper.getMainLooper()).postDelayed({
            var intent: Intent? = null
            // do you work now
            if (isLock) {
                intent = Intent(this@SplashActivity, PasscodeActivity::class.java)
            } else {
                intent = Intent(this@SplashActivity, MainActivity::class.java)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2000)
    }
}