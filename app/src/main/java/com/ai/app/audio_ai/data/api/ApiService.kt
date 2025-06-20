package com.ai.app.audio_ai.data.api

import com.ai.app.audio_ai.data.model.AudioContentDetailResponse
import com.ai.app.audio_ai.data.model.AudioContentResponse
import com.ai.app.audio_ai.data.model.BannerResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    /**
     * 获取轮播图数据
     */
    @GET("banners")
    suspend fun getBanners(): BannerResponse

    /**
     * 获取热门推荐数据
     * @param limit 返回数量限制
     * @param offset 偏移量，用于分页
     */
    @GET("recommendations/hot")
    suspend fun getHotRecommendations(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): AudioContentResponse

    /**
     * 获取最新上架数据
     * @param limit 返回数量限制
     * @param offset 偏移量，用于分页
     */
    @GET("recommendations/new")
    suspend fun getNewReleases(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): AudioContentResponse

    /**
     * 获取音频内容详情
     * @param id 音频内容ID
     */
    @GET("audio/{id}")
    suspend fun getAudioContentDetail(
        @Path("id") id: Int
    ): AudioContentDetailResponse

    /**
     * 搜索音频内容
     * @param keyword 搜索关键词
     * @param limit 返回数量限制
     * @param offset 偏移量，用于分页
     */
    @GET("search")
    suspend fun searchAudioContent(
        @Query("keyword") keyword: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): AudioContentResponse

    /**
     * 获取分类列表
     */
    @GET("categories")
    suspend fun getCategories(): AudioContentResponse

    /**
     * 获取分类下的音频内容
     * @param category 分类名称
     * @param limit 返回数量限制
     * @param offset 偏移量，用于分页
     */
    @GET("category/{category}")
    suspend fun getCategoryAudioContents(
        @Path("category") category: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): AudioContentResponse
}