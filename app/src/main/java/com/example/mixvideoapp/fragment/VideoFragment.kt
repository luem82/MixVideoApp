package com.example.mixvideoapp.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentVideoBinding
import com.example.mixvideoapp.util.Consts

private const val ARG_PARAM1 = "param1"

class VideoFragment : Fragment() {

    private var mType: String? = null
    private lateinit var binding: FragmentVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mType = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        binding = FragmentVideoBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            VideoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, type)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mType) {
            Consts.TYPE_VIET -> {
                replaceFragment(mType!!, "uploaddate")
            }
            Consts.TYPE_TRAN -> {
                replaceFragment(mType!!, "all-recent")
            }
            Consts.TYPE_ANI -> {
                replaceFragment(mType!!, "best")
            }
            Consts.TYPE_EN -> {
                replaceFragment(mType!!, "latest-updates")
            }
        }

        binding.chipVideoNew.setOnClickListener {
            when (mType) {
                Consts.TYPE_VIET -> {
                    replaceFragment(mType!!, "uploaddate")
                }
                Consts.TYPE_TRAN -> {
                    replaceFragment(mType!!, "all-recent")
                }
                Consts.TYPE_ANI -> {
                    replaceFragment(mType!!, "best")
                }
                Consts.TYPE_EN -> {
                    replaceFragment(mType!!, "latest-updates")
                }
            }
        }

        binding.chipVideoPopular.setOnClickListener {
            when (mType) {
                Consts.TYPE_VIET -> {
                    replaceFragment(mType!!, "views")
                }
                Consts.TYPE_TRAN -> {
                    replaceFragment(mType!!, "all-popular")
                }
                Consts.TYPE_ANI -> {
                    replaceFragment(mType!!, "new")
                }
                Consts.TYPE_EN -> {
                    replaceFragment(mType!!, "most-popular")
                }
            }
        }

        binding.chipVideoRating.setOnClickListener {
            when (mType) {
                Consts.TYPE_VIET -> {
                    replaceFragment(mType!!, "rating")
                }
                Consts.TYPE_TRAN -> {
                    replaceFragment(mType!!, "all-rate")
                }
                Consts.TYPE_ANI -> {
                    replaceFragment(mType!!, "toprated")
                }
                Consts.TYPE_EN -> {
                    replaceFragment(mType!!, "top-rated")
                }
            }
        }
    }

    private fun replaceFragment(type: String, sort: String) {
        val fragment = ListVideoFragment.newInstance(type, sort, "")
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_video, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }
}