package com.ai.app.audio_ai.di

import com.ai.app.audio_ai.data.remote.ApiClient
import com.ai.app.audio_ai.data.remote.AudioApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        // Empty companion object to ensure compatibility
    }

    @Provides
    @Singleton
    fun provideApiClient(): ApiClient {
        return ApiClient()
    }

    @Provides
    @Singleton
    fun provideAudioApiService(apiClient: ApiClient): AudioApiService {
        return apiClient.audioApiService
    }
}
