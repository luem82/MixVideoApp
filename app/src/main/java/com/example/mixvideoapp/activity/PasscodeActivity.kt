package com.example.mixvideoapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.ActivityPasscodeBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.PreferenceManager
import com.hanks.passcodeview.PasscodeView

class PasscodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasscodeBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        val passCode = preferenceManager.getPassCode(Consts.PRE_KEY_PASS)

        binding.passcodeView.setLocalPasscode(passCode)
            .setPasscodeLength(passCode!!.length)
            .setFirstInputTip("Nhập Mật Mã " + passCode.length + " Chữ Số.")
            .setListener(object : PasscodeView.PasscodeViewListener {
                override fun onFail() {
                    Toast.makeText(
                        this@PasscodeActivity, R.string.passcode_wrong, Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onSuccess(number: String?) {
                    startActivity(Intent(this@PasscodeActivity, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            })

    }
}