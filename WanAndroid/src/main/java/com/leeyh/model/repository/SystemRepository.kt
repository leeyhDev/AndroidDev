package com.leeyh.model.repository

import com.leeyh.model.api.WanRetrofitClient
import com.leeyh.model.bean.Article
import com.leeyh.model.bean.SystemType
import com.leeyh.model.bean.WanData

class SystemRepository : BaseRepository() {
    suspend fun getSystem(): WanData<List<SystemType>> {
        return getDataCall { WanRetrofitClient.service.getSystem() }
    }

    suspend fun getSystemList(page: Int, cid: Int): WanData<List<Article>> {
        return getDataCall { WanRetrofitClient.service.getSystemList(page, cid) }
    }
}