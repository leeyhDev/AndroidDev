package com.leeyh.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.BarUtils
import com.leeyh.R
import com.leeyh.ui.article.ArticleFragment
import com.leeyh.ui.note.NoteFragment
import com.leeyh.ui.system.SystemFragment
import com.tablayout.bean.CustomTabEntity
import com.tablayout.listener.OnTabSelectListener
import kotlinx.android.synthetic.main.note_main_activity.*

class NoteMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_main_activity)
        BarUtils.setStatusBarColor(this, getColor(R.color.colorPrimary))
        val tabEntities = arrayListOf(
            CustomTabEntity("首页", R.drawable.ic_home_selected, R.drawable.ic_home_normal),
            CustomTabEntity("体系", R.drawable.ic_system_selected, R.drawable.ic_system_normal),
            CustomTabEntity("笔记", R.drawable.ic_note_selected, R.drawable.ic_note_normal),
            CustomTabEntity("我的", R.drawable.ic_me_selected, R.drawable.ic_me_normal)
        )
        val fragments = arrayOf(ArticleFragment(), SystemFragment(), NoteFragment(), NoteFragment())
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
