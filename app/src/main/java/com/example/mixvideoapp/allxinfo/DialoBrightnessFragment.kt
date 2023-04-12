package com.example.mixvideoapp.allxinfo

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.CustomDialogBrightnessBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialoBrightnessFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = MaterialAlertDialogBuilder(requireActivity())
        val layoutInflater = requireActivity().layoutInflater
        val view: View = layoutInflater.inflate(R.layout.custom_dialog_brightness, null)
        val binding = CustomDialogBrightnessBinding.bind(view)
        builder.setView(view)
        builder.background = ColorDrawable(Color.TRANSPARENT)


        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        var brightnessActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data = result.data
//                            doSomeOperations();
            }
        }

        val brightness = Settings.System.getInt(
            requireContext().contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, 0
        )
        binding.tvNum.text = "$brightness %"
        binding.seekBar.progress = brightness
        if (binding.seekBar.progress == 0) {
            binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_low_24)
        } else if (binding.seekBar.progress > 0 && binding.seekBar.progress < 51) {
            binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_medium_24)
        } else {
            binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_high_24)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val context = requireContext().applicationContext
                val canWrite = Settings.System.canWrite(context)
                if (canWrite) {
                    val sBright = progress * 255 / 255
                    binding.tvNum.text = "$sBright %"
                    Log.e("brightness", "onProgressChanged: ${progress}")
                    if (progress == 0) {
                        binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_low_24)
                    } else if (progress > 0 && progress < 51) {
                        binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_medium_24)
                    } else {
                        binding.ivBrit.setImageResource(R.drawable.ic_baseline_brightness_high_24)
                    }

                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                    )
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS, sBright
                    )
                } else {
                    Toast.makeText(
                        context, "Bật cài đặt ghi để kiểm soát độ sáng.", Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:" + context.packageName)
                    brightnessActivityResultLauncher.launch(intent)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        binding.ibtnClose.setOnClickListener { v -> dismiss() }

        return builder.create()
    }

    private fun setBrightnessNumber(
        audioManager: AudioManager,
        binding: CustomDialogBrightnessBinding
    ) {
        val mediaVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPer = Math.ceil(mediaVol.toDouble() / maxVol.toDouble() * 100.toDouble())
        val num = volPer.toString().replace(".0", "") + " %"
        binding.tvNum.text = num
    }
}