package com.leeyh.model.repository

import com.leeyh.model.bean.Result
import com.leeyh.model.bean.WanData
import java.io.IOException


open class BaseRepository {
    suspend fun <T : Any> getDataCall(call: suspend () -> WanData<T>): WanData<T> {
        return call()
    }

    suspend fun <T : Any> safeApiCall(call: suspend () -> Result<T>, errorMessage: String): Result<T> {
        return try {
            call()
        } catch (throwable: Throwable) {
            // An exception was thrown when calling the API so we're converting this to an IOException
            Result.Error(IOException(errorMessage, throwable))
        }
    }
}