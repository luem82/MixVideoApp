package com.example.mixvideoapp.lesbian8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentLes8VideoBinding

class Les8VideoFragment : Fragment() {

    private lateinit var binding: FragmentLes8VideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_les8_video, container, false)
        binding = FragmentLes8VideoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = Les8VideoFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        replaceFragment("latest-updates")

        binding.chipVideoNew.setOnClickListener {
            replaceFragment("latest-updates")
        }

        binding.chipVideoPopular.setOnClickListener {
            replaceFragment("most-popular")
        }
    }

    private fun replaceFragment(sort: String) {
        val fragment = Les8ListVideoFragment.newInstance(sort)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_video_les, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }
}