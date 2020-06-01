package com.leeyh.note.ui.system

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.alibaba.android.arouter.facade.annotation.Route
import com.core.base.BaseActivity
import com.core.constant.ParamKey
import com.core.constant.ParamValue
import com.core.constant.RouterPath
import com.leeyh.R
import com.leeyh.model.bean.SystemType
import com.leeyh.note.ui.article.ArticleListFragment
import kotlinx.android.synthetic.main.system_list_activity.*

@Route(path = RouterPath.SystemList)
class SystemListActivity : BaseActivity() {

    override fun getLayoutResId(): Int = R.layout.system_list_activity

    override fun initView() {
        viewPager.offscreenPageLimit = 5
    }

    override fun initData() {
        val systemChildren = intent.getParcelableExtra<SystemType>(ParamKey.SYSTEM_TYPE)
        val position = intent.getIntExtra(ParamKey.POSITION, 0)
        systemChildren?.let {
            val fragments = ArrayList<Fragment>()
            it.children.forEach { child ->
                fragments.add(ArticleListFragment.newInstance(ParamValue.ARTICLE_SYSTEM, child.id))
            }
            viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int) = ArticleListFragment.newInstance(ParamValue.ARTICLE_SYSTEM, it.children[position].id)

                override fun getCount() = it.children.size

                override fun getPageTitle(position: Int) = it.children[position].name
            }
            tabLayout.setViewPager(viewPager)
            tabLayout.currentTab = position
        }
    }
}
