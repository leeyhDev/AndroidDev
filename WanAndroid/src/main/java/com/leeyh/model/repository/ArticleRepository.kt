package com.leeyh.model.repository

import com.leeyh.model.api.WanRetrofitClient
import com.leeyh.model.bean.ArticleList
import com.leeyh.model.bean.Result
import com.leeyh.model.bean.WanData
import com.leeyh.model.bean.WxArticleChapter

class ArticleRepository : BaseRepository() {

    suspend fun getWxArticleChapters(): WanData<List<WxArticleChapter>> {
        return getDataCall { WanRetrofitClient.service.getWxArticleChapters() }
    }

    suspend fun getArticleList(path: String, page: Int): WanData<ArticleList> {
        return getDataCall { WanRetrofitClient.service.getArticleList(path, page) }
    }

    suspend fun getWxArticleList(id: Int, page: Int): WanData<ArticleList> {
        return getDataCall { WanRetrofitClient.service.getWxArticleList(id, page) }
    }

    suspend fun getArticleSystemList(page: Int, cid: Int): WanData<ArticleList> {
        return getDataCall(call = { WanRetrofitClient.service.getArticleSystemList(page, cid) })
    }

    private suspend fun requestSquareArticleList(page: Int, cid: Int): Result<ArticleList> {
        val response = WanRetrofitClient.service.getArticleSystemList(page, cid)
        return if (response.errorCode == 0) Result.Success(response.data)
        else Result.Error(Throwable(response.errorMsg))
    }
}