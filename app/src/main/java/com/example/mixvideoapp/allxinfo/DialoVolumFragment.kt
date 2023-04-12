package com.example.mixvideoapp.allxinfo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.CustomDialogVolumBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialoVolumFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = MaterialAlertDialogBuilder(requireActivity())
        val layoutInflater = requireActivity().layoutInflater
        val view: View = layoutInflater.inflate(R.layout.custom_dialog_volum, null)
        val binding: CustomDialogVolumBinding = CustomDialogVolumBinding.bind(view)
        builder.setView(view)
        builder.background = ColorDrawable(Color.TRANSPARENT)


        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        binding.seekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.seekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        setVolumNumber(audioManager, binding)
        if (binding.seekBar.progress == 0) {
            binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_off_24)
        } else if (binding.seekBar.progress > 0 && binding.seekBar.progress < 7) {
            binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_down_24)
        } else {
            binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_up_24)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                setVolumNumber(audioManager, binding)
                Log.e("progressvolum", "onProgressChanged: ${progress}")
                if (progress == 0) {
                    binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_off_24)
                } else if (progress > 0 && progress < 7) {
                    binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_down_24)
                } else {
                    binding.ivSound.setImageResource(R.drawable.ic_baseline_volume_up_24)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        binding.ibtnClose.setOnClickListener { v -> dismiss() }

        return builder.create()
    }

    private fun setVolumNumber(audioManager: AudioManager, binding: CustomDialogVolumBinding) {
        val mediaVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volPer = Math.ceil(mediaVol.toDouble() / maxVol.toDouble() * 100.toDouble())
        val num = volPer.toString().replace(".0", "") + " %"
        binding.tvNum.text = num
    }
}