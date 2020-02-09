package com.leeyh.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.leeyh.R

class WanAndroidWXArticleFragment : Fragment() {

    companion object {
        fun newInstance() = WanAndroidWXArticleFragment()
    }

    private lateinit var viewModel: WanAndroidWXArticleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.wan_android_fragment_wx_article, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(WanAndroidWXArticleViewModel::class.java)
    }

}
