package com.example.mixvideoapp.xcuto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentCutoBinding

class CutoFragment : Fragment() {

    private lateinit var binding: FragmentCutoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cuto, container, false)
        binding = FragmentCutoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = CutoFragment()
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

        binding.chipVideoRating.setOnClickListener {
            replaceFragment("top-rated")
        }
    }

    private fun replaceFragment(sort: String) {
        val fragment = CutoListVideoFragment.newInstance(sort)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_video_cuto, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }
}