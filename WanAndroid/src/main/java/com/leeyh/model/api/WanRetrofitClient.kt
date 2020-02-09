package com.leeyh.model.api

import com.core.api.BaseRetrofitClient

object WanRetrofitClient : BaseRetrofitClient() {

    val service by lazy {
        getService(WanService::class.java, WanService.BASE_URL)
    }
}