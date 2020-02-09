package com.leeyh.ui.article

import android.util.Log
import com.core.base.BaseFragment
import com.leeyh.R

class WanAndroidHomeFragment : BaseFragment() {

    companion object {
        fun newInstance() = WanAndroidHomeFragment()
    }

    private lateinit var viewModel: WanAndroidHomeViewModel


    override fun getLayoutResId(): Int = R.layout.wan_android_fragment_home

    override fun initView() {
        Log.d("111","WanAndroidHomeFragment")
    }

    override fun initData() {
    }
}
