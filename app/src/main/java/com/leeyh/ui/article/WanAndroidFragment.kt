package com.leeyh.ui.article

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.core.base.BaseFragment
import com.leeyh.R
import kotlinx.android.synthetic.main.wan_android_fragment.*

class WanAndroidFragment : BaseFragment() {

    private lateinit var wanAndroidViewModel: WanAndroidViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wanAndroidViewModel = ViewModelProvider(this).get(WanAndroidViewModel::class.java)
    }

    override fun getLayoutResId(): Int = R.layout.wan_android_fragment

    override fun initView() {
        Log.d("111", "wan")
    }

    override fun initData() {
        val fragments = arrayListOf(
            WanAndroidHomeFragment.newInstance(),
            WanAndroidProjectFragment.newInstance(),
            WanAndroidWXArticleFragment.newInstance()
        )
        val title = arrayOf("首页", "项目", "微信公众号")
        viewPager.offscreenPageLimit = fragments.size
        viewPager.adapter = object : FragmentPagerAdapter(parentFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment = fragments[position]
            override fun getCount(): Int = fragments.size
            override fun getPageTitle(position: Int): CharSequence? = title[position]
        }
        tabLayout.setupWithViewPager(viewPager)
    }
}