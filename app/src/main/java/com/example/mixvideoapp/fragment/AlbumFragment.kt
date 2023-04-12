package com.example.mixvideoapp.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentAlbumBinding
import com.example.mixvideoapp.util.Consts

class AlbumFragment : Fragment() {

    private lateinit var binding: FragmentAlbumBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_album, container, false)
        binding = FragmentAlbumBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = AlbumFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.act_search).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        replaceFragment(Consts.TYPE_VIET)

        binding.albumViet.setOnClickListener { replaceFragment(Consts.TYPE_VIET) }
        binding.albumEn.setOnClickListener { replaceFragment(Consts.TYPE_EN) }
        binding.albumTran.setOnClickListener { replaceFragment(Consts.TYPE_TRAN) }
        binding.albumAni.setOnClickListener { replaceFragment(Consts.TYPE_ANI) }
    }

    private fun replaceFragment(type: String) {
        val fragment = ListAlbumFragment.newInstance(type)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_album, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }
}