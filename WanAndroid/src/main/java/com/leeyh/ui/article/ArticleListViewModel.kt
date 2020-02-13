package com.leeyh.ui.article

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.core.base.BaseViewModel
import com.leeyh.model.bean.ArticleList
import com.leeyh.model.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArticleListViewModel : BaseViewModel() {
    private val repository by lazy { ArticleRepository() }
    val articleList: MutableLiveData<ArticleList> = MutableLiveData()

    fun getArticleList(article: String, page: Int) {
        launch {
            val result = withContext(Dispatchers.IO) {
                repository.getArticleList(article, page)
            }
            if (result.errorCode == 0) {
                articleList.value = result.data
            } else {
                LogUtils.d(result.errorMsg)
            }
        }
    }

    fun getWxArticleList(id: Int, page: Int) {
        launch {
            val result = withContext(Dispatchers.IO) {
                repository.getWxArticleList(id, page)
            }
            if (result.errorCode == 0) {
                articleList.value = result.data
            } else {
                LogUtils.d(result.errorMsg)
            }
        }
    }

    fun getArticleSystemList(cid: Int, page: Int) {
        launch {
            val result = withContext(Dispatchers.IO) {
                repository.getArticleSystemList(page, cid)
            }
             if (result.errorCode == 0) {
                 articleList.value = result.data
             } else {
                 LogUtils.d(result.errorMsg)
             }
        }
    }
}