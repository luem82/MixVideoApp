package com.example.mixvideoapp.lesbian8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.animaltube.network.NetworkBasic
import com.example.mixvideoapp.R
import com.example.mixvideoapp.databinding.FragmentLes8CategoryBinding
import com.example.mixvideoapp.util.Consts
import com.example.mixvideoapp.util.ParseHTML
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class Les8CategoryFragment : Fragment() {

    private lateinit var binding: FragmentLes8CategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_les8_category, container, false)
        binding = FragmentLes8CategoryBinding.bind(view)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = Les8CategoryFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val les8CategoryAdapter = Les8CategoryAdapter(R.layout.item_category_les, null)
        les8CategoryAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM)
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.adapter = les8CategoryAdapter

        NetworkBasic.getRetrofit(Consts.URL_LES8).create(Les8Api::class.java)
            .getAllCategories().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableObserver<String>() {
                override fun onNext(html: String) {
                    val data = ParseHTML.parseCategories(Consts.TYPE_LES8, html)
                    les8CategoryAdapter.setNewData(data)
                }

                override fun onError(e: Throwable) {
                    binding.pbCate.visibility = View.GONE
                    Toast.makeText(context, "Lỗi: $${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                    binding.pbCate.visibility = View.GONE
                }
            })
    }
}