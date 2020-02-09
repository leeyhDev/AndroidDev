package com.leeyh.model.repository

import com.leeyh.model.api.WanRetrofitClient
import com.leeyh.model.bean.ArticleList
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
}