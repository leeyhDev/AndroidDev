package com.leeyh.note.ui.article

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.core.base.BaseVMFragment
import com.core.constant.ParamKey
import com.core.constant.ParamValue
import com.leeyh.R
import com.leeyh.model.bean.ArticleList
import kotlinx.android.synthetic.main.article_list_fragment.*

class ArticleListFragment : BaseVMFragment<ArticleListViewModel>() {
    override fun providerVMClass(): Class<ArticleListViewModel>? = ArticleListViewModel::class.java
    private lateinit var articleAdapter: ArticleAdapter
    private var currentPage = 0
    private lateinit var articleType: String
    private var articleCid = 0

    companion object {
        fun newInstance(path: String = ParamValue.ARTICLE_NEW, cid: Int = 0) =
            ArticleListFragment().apply {
                arguments = Bundle().apply {
                    putString(ParamKey.ARTICLE_TYPE, path)
                    putInt(ParamKey.ARTICLE_CID, cid)
                }
            }
    }

    override fun getLayoutResId(): Int = R.layout.article_list_fragment

    override fun initView() {
        articleRv.run {
            layoutManager = LinearLayoutManager(context)
        }
        initAdapter()
        initRefreshLayout()
        initLoadMore()
    }

    override fun initData() {
        arguments?.let { bundle ->
            articleType = bundle.getString(ParamKey.ARTICLE_TYPE, ParamValue.ARTICLE_NEW)
            articleCid = bundle.getInt(ParamKey.ARTICLE_CID)
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
        when (articleType) {
            ParamValue.ARTICLE_NEW -> {
                currentPage = 0
                viewModel.getArticleList("article", currentPage)
            }
            ParamValue.ARTICLE_USER -> {
                currentPage = 0
                viewModel.getArticleList("user_article", currentPage)
            }
            ParamValue.ARTICLE_WX -> {
                currentPage = 1
                viewModel.getWxArticleList(articleCid, currentPage)
            }
            ParamValue.ARTICLE_SYSTEM -> {
                currentPage = 0
                viewModel.getArticleSystemList(articleCid, currentPage)
            }
        }
    }

    private fun loadMore() {
        if (StringUtils.isEmpty(articleType)) {
            viewModel.getWxArticleList(articleCid, currentPage)
        } else {
            viewModel.getArticleList(articleType, currentPage)
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
