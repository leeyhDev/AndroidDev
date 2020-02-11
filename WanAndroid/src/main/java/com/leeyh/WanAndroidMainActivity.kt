package com.leeyh

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.leeyh.ui.article.ArticleFragment
import com.leeyh.ui.system.SystemFragment
import com.tablayout.bean.CustomTabEntity
import com.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.wan_android_main_activity.*

class WanAndroidMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wan_android_main_activity)
        val tabEntities = arrayListOf(
            CustomTabEntity("首页", R.mipmap.home, R.mipmap.home_pre),
            CustomTabEntity("体系", R.mipmap.system, R.mipmap.system_pre)
        )
        val fragments = arrayOf(ArticleFragment(), SystemFragment())
        commonTabLayout.setTabData(tabEntities)
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int) = fragments[position]
            override fun getCount() = fragments.size
            override fun getPageTitle(position: Int) = tabEntities[position].tabTitle
        }
        commonTabLayout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                viewPager.setCurrentItem(position, false)
            }

            override fun onTabReselect(position: Int) {}
        })
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                commonTabLayout.currentTab = position
            }
        })
        viewPager.currentItem = 0
    }
}

