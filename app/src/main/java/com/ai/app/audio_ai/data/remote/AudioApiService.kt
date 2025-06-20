package com.ai.app.audio_ai.data.remote

import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.data.model.Banner
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AudioApiService {
    @GET("banners")
    suspend fun getBanners(): List<Banner>

    @GET("recommendations/hot")
    suspend fun getHotRecommendations(): List<AudioContent>

    @GET("recommendations/new")
    suspend fun getNewReleases(): List<AudioContent>

    @GET("audio/{id}")
    suspend fun getAudioContentById(@Path("id") id: String): AudioContent?

    @GET("search")
    suspend fun searchAudioContent(@Query("q") query: String): List<AudioContent>
}
