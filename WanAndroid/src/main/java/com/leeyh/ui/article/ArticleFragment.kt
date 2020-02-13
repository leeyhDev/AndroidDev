package com.leeyh.ui.article

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.LogUtils
import com.core.base.BaseFragment
import com.core.constant.ParamValue
import com.leeyh.R
import com.leeyh.model.repository.ArticleRepository
import kotlinx.android.synthetic.main.article_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleFragment : BaseFragment() {

    private val titleArray = ArrayList<String>()
    private val articleFragments = ArrayList<Fragment>()

    override fun getLayoutResId(): Int = R.layout.article_fragment

    override fun initView() {
        viewPager.offscreenPageLimit = 5
    }

    override fun initData() {
        titleArray.add("最新")
        titleArray.add("广场")
        articleFragments.add(ArticleListFragment.newInstance(ParamValue.ARTICLE_NEW))
        articleFragments.add(ArticleListFragment.newInstance(ParamValue.ARTICLE_USER))
        launch {
            val result = withContext(Dispatchers.IO) {
                ArticleRepository().getWxArticleChapters()
            }
            if (result.errorCode == 0) {
                result.data.forEach {
                    titleArray.add(it.name)
                    articleFragments.add(ArticleListFragment.newInstance(ParamValue.ARTICLE_WX, it.id))
                }
                viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                    override fun getItem(position: Int) = articleFragments[position]
                    override fun getCount() = titleArray.size
                    override fun getPageTitle(position: Int) = titleArray[position]
                }
                tabLayout.setViewPager(viewPager)
            } else {
                LogUtils.d(result.errorMsg)
            }
        }
    }

}