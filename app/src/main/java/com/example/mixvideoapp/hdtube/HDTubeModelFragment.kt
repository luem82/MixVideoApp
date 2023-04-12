package com.example.mixvideoapp.hdtube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentHDTubeModelBinding
import com.google.android.material.button.MaterialButton
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class HDTubeModelFragment : Fragment(), HDTubeModelAdapter.HDTubeModelListener {

    private lateinit var binding: FragmentHDTubeModelBinding
    private var mPage = 1
    private var mGender = "all"
    private var mLetter = ""
    private lateinit var mAdapter: HDTubeModelAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_h_d_tube_model, container, false)
        binding = FragmentHDTubeModelBinding.bind(view)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = HDTubeModelFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        loadData()
        setListeners()
    }

    private fun setListeners() {
        binding.btnFilterLetter.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            for (i in HDConsts.LIST_LETTER.indices) {
                popupMenu.menu.add(Menu.NONE, i, i, HDConsts.LIST_LETTER.get(i))
            }
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                val positon: Int = it.getItemId()
                val letter: String = HDConsts.LIST_LETTER.get(positon)
                val gender = binding.btnFilterGender.text.toString().substringAfter(":").trim()
                binding.btnFilterLetter.text = "A - Z: " + letter
                filterModels(letter, gender)
                true
            }
        }

        binding.btnFilterGender.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            for (i in HDConsts.LIST_GENDER.indices) {
                popupMenu.menu.add(Menu.NONE, i, i, HDConsts.LIST_GENDER.get(i))
            }
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                val positon: Int = it.getItemId()
                val gender: String = HDConsts.LIST_GENDER.get(positon)
                val letter = binding.btnFilterLetter.text.toString().substringAfter(":").trim()
                binding.btnFilterGender.text = "Gender: " + gender
                filterModels(letter, gender)
                true
            }
        }
    }

    private fun filterModels(letter: String, gender: String) {
        mPage = 1
        mGender = gender
        if (letter.equals("all")) {
            mLetter = ""
        } else {
            mLetter = letter
        }
        Log.e("filterModels", "gender: ${mGender} - letter: ${mLetter}")
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
        binding.pbModel.visibility = View.VISIBLE
        loadData()
    }

    private fun initAdapter() {
        mAdapter = HDTubeModelAdapter(R.layout.item_model_hdtube_big, null, this)
        mAdapter.setOnLoadMoreListener({
            mPage = mPage + 1
            loadData()
        }, binding.rvModel)
        mAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvModel.setHasFixedSize(true)
        binding.rvModel.adapter = mAdapter
        mAdapter.setEnableLoadMore(true)
    }

    private fun loadData() {
        val isRefresh = mPage == 1
        NetworkBasic.getRetrofit(HDConsts.BASE_URL).create(HDTubeApi::class.java)
            .getHtmlModels(mPage, mLetter, mGender)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    var data = ParseHDTube.parseModels(html)
                    if (isRefresh) {
                        mAdapter.setNewData(data)
                        mAdapter.setEnableLoadMore(true)
                    } else {
                        mAdapter.addData(data)
                    }
                    if (data.size < 1) {
                        mAdapter.loadMoreEnd(true)
                    } else if (!isRefresh) {
                        mAdapter.loadMoreComplete()
                    }
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    Log.e("TAG", "Load more error: ${e.message}")
                    if (e.message!!.contains("HTTP 404")) {
                        mAdapter.loadMoreEnd(true)
                    }
                }

                override fun onComplete() {
                    binding.pbModel.visibility = View.INVISIBLE
                }
            })
    }

    override fun onModelClick(hdTubeModel: HDTubeModel) {
        val intent = Intent(context, HDTubeModelDetailActivity::class.java)
        intent.putExtra("HDTubeModel", hdTubeModel)
        startActivity(intent)
    }
}