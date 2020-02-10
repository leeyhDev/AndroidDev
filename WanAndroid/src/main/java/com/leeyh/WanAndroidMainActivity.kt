package com.leeyh

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.leeyh.ui.article.ArticleFragment
import com.leeyh.ui.dashboard.DashboardFragment
import com.tablayout.listener.CustomTabEntity
import com.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.wan_android_main_activity.*
import java.util.*

class WanAndroidMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wan_android_main_activity)
        val tabEntities = getTabs()
        val fragments = arrayOf(ArticleFragment(), DashboardFragment())
        tabLayout.setTabData(tabEntities)
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int) = fragments[position]
            override fun getCount() = tabEntities.size
            override fun getPageTitle(position: Int) = tabEntities[position].tabTitle
        }
        tabLayout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) = viewPager.setCurrentItem(position, false)
            override fun onTabReselect(position: Int) {}
        })
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                tabLayout.currentTab = position
            }
        })
    }

    private fun getTabs(): ArrayList<CustomTabEntity> {
        val homeTab = TabEntity("首页", R.mipmap.home_pre, R.mipmap.home)
        val communityTab = TabEntity("笔记", R.mipmap.note_pre, R.mipmap.note)
        val tabEntities = ArrayList<CustomTabEntity>()
        tabEntities.add(homeTab)
        tabEntities.add(communityTab)
        return tabEntities
    }
}
