package com.ai.app.audio_ai.di

import com.ai.app.audio_ai.data.repository.AudioRepository
import com.ai.app.audio_ai.data.repository.AudioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAudioRepository(impl: AudioRepositoryImpl): AudioRepository {
        return impl
    }
}
