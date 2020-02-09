package com.leeyh.model.api

import com.leeyh.model.bean.ArticleList
import com.leeyh.model.bean.WanData
import com.leeyh.model.bean.WxArticleChapter
import retrofit2.http.GET
import retrofit2.http.Path

interface WanService {

    companion object {
        const val BASE_URL = "https://www.wanandroid.com"
    }

    @GET("/wxarticle/chapters/json")
    suspend fun getWxArticleChapters(): WanData<List<WxArticleChapter>>

    @GET("/{articlePath}/list/{page}/json")
    suspend fun getArticleList(@Path("articlePath") path: String, @Path("page") page: Int): WanData<ArticleList>

    @GET("wxarticle/list/{id}/{page}/json")
    suspend fun getWxArticleList(@Path("id") id: Int, @Path("page") page: Int): WanData<ArticleList>

    /* @GET("/banner/json")
     suspend fun getBanner(): WanBaseData<List<Banner>>

     @GET("/tree/json")
     suspend fun getSystemType(): WanBaseData<List<SystemParent>>

     @GET("/article/list/{page}/json")
     suspend fun getSystemTypeDetail(@Path("page") page: Int, @Query("cid") cid: Int): WanBaseData<ArticleList>

     @GET("/navi/json")
     suspend fun getNavigation(): WanBaseData<List<Navigation>>

     @GET("/project/tree/json")
     suspend fun getProjectType(): WanBaseData<List<SystemParent>>

     @GET("/wxarticle/chapters/json")
     suspend fun getBlogType(): WanBaseData<List<SystemParent>>

     @GET("/wxarticle/list/{id}/{page}/json")
     fun getBlogArticle(@Path("id") id: Int, @Path("page") page: Int): WanBaseData<ArticleList>

     @GET("/project/list/{page}/json")
     suspend fun getProjectTypeDetail(@Path("page") page: Int, @Query("cid") cid: Int): WanBaseData<ArticleList>

     @GET("/article/listproject/{page}/json")
     suspend fun getLastedProject(@Path("page") page: Int): WanBaseData<ArticleList>

     @GET("/friend/json")
     suspend fun getWebsites(): WanBaseData<List<Hot>>

     @GET("/hotkey/json")
     suspend fun getHot(): WanBaseData<List<Hot>>

     @FormUrlEncoded
     @POST("/article/query/{page}/json")
     suspend fun searchHot(@Path("page") page: Int, @Field("k") key: String): WanBaseData<ArticleList>

     @FormUrlEncoded
     @POST("/user/login")
     suspend fun login(@Field("username") userName: String, @Field("password") passWord: String): WanBaseData<User>

     @GET("/user/logout/json")
     suspend fun logOut(): WanBaseData<Any>

     @FormUrlEncoded
     @POST("/user/register")
     suspend fun register(@Field("username") userName: String, @Field("password") passWord: String, @Field("repassword") rePassWord: String): WanBaseData<User>

     @GET("/lg/collect/list/{page}/json")
     suspend fun getCollectArticles(@Path("page") page: Int): WanBaseData<ArticleList>

     @POST("/lg/collect/{id}/json")
     suspend fun collectArticle(@Path("id") id: Int): WanBaseData<ArticleList>

     @POST("/lg/uncollect_originId/{id}/json")
     suspend fun cancelCollectArticle(@Path("id") id: Int): WanBaseData<ArticleList>

     @GET("/user_article/list/{page}/json")
     suspend fun getSquareArticleList(@Path("page") page: Int): WanBaseData<ArticleList>

     @FormUrlEncoded
     @POST("/lg/user_article/add/json")
     suspend fun shareArticle(@Field("title") title: String, @Field("link") url: String): WanBaseData<String>*/

}