package com.example.mixvideoapp.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        binding = FragmentCategoryBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = CategoryFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.act_search).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        replaceFragment("vn_cate", "https://zbporn.tv/categories/")

        binding.cateViet.setOnClickListener {
            replaceFragment("vn_cate", "https://zbporn.tv/categories/")
        }

        binding.cateEn.setOnClickListener {
            replaceFragment("en_cate", "https://zbporn.tv/categories/")
        }

        binding.cateTran.setOnClickListener {
            replaceFragment("tran_cate", "https://www.shemaletubevideos.com/categories/")
        }

        binding.cateAni.setOnClickListener {
            replaceFragment("ani_cate", "https://zoozoosexporn.com/en/cats.php")
        }
    }

    private fun replaceFragment(type: String, url: String) {
        val fragment = ListCategoryFragment.newInstance(type, url)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container_category, fragment)
        fragmentTransaction.disallowAddToBackStack()
        fragmentTransaction.commit()
    }
}