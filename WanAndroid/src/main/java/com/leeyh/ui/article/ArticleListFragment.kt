package com.leeyh.ui.article

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.core.SpaceItemDecoration
import com.core.base.BaseVMFragment
import com.core.ui.BrowserActivity
import com.leeyh.R
import com.leeyh.model.bean.ArticleList
import kotlinx.android.synthetic.main.article_list_fragment.*

class ArticleListFragment : BaseVMFragment<ArticleListViewModel>() {
    override fun providerVMClass(): Class<ArticleListViewModel>? = ArticleListViewModel::class.java
    private lateinit var articleAdapter: ArticleAdapter
    private var currentPage = 0
    private lateinit var articlePath: String
    private var articleId = 408

    companion object {
        const val ARTICLE_PATH = "articlePath"
        const val WX_ARTICLE_ID = "wxArticleId"
        fun newInstance(type: String, id: Int) =
            ArticleListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARTICLE_PATH, type)
                    putInt(WX_ARTICLE_ID, id)
                }
            }
    }

    override fun getLayoutResId(): Int = R.layout.article_list_fragment

    override fun initView() {
        articleRv.run {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(SpaceItemDecoration(SizeUtils.dp2px(12f)))
        }
        initAdapter()
        initRefreshLayout()
        initLoadMore()
    }

    override fun initData() {
        arguments?.let { bundle ->
            articlePath = bundle.getString(ARTICLE_PATH, "article")
            articleId = bundle.getInt(WX_ARTICLE_ID, 408)
            refresh()
        }
    }

    private fun initRefreshLayout() {
        swipeRefreshLayout.setColorSchemeColors(ColorUtils.getColor(R.color.colorPrimary))
        swipeRefreshLayout.setOnRefreshListener { refresh() }
    }

    /**
     * 初始化加载更多
     */
    private fun initLoadMore() {
        articleAdapter.loadMoreModule?.let {
            it.setOnLoadMoreListener { loadMore() }
            it.isAutoLoadMore = true
            //当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多(默认为true)
            it.isEnableLoadMoreIfNotFullPage = false
        }
    }

    private fun initAdapter() {
        articleAdapter = ArticleAdapter(R.layout.item_home, viewModel.articleList.value?.datas)
        articleRv.adapter = articleAdapter
    }

    private fun refresh() {
        articleAdapter.loadMoreModule?.isEnableLoadMore = false
        swipeRefreshLayout.isRefreshing = true
        if (StringUtils.isEmpty(articlePath)) {
            currentPage = 1
            viewModel.getWxArticleList(articleId, currentPage)
        } else {
            currentPage = 0
            viewModel.getArticleList(articlePath, currentPage)
        }
    }

    private fun loadMore() {
        if (StringUtils.isEmpty(articlePath)) {
            viewModel.getWxArticleList(articleId, currentPage)
        } else {
            viewModel.getArticleList(articlePath, currentPage)
        }
    }

    override fun startObserve() {
        super.startObserve()
        viewModel.apply {
            articleList.observe(this@ArticleListFragment, Observer { it ->
                it?.let { setArticles(it) }
            })
        }
    }

    private fun setArticles(articleList: ArticleList) {
        articleAdapter.run {
            if (swipeRefreshLayout.isRefreshing) replaceData(articleList.datas)
            else addData(articleList.datas)
            articleAdapter.loadMoreModule?.let {
                it.isEnableLoadMore = true
                it.loadMoreComplete()
            }
        }
        swipeRefreshLayout.isRefreshing = false
        currentPage++
    }
}
