package com.ai.app.audio_ai.data.repository

import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.data.model.Banner

interface AudioRepository {
    fun getAudioContent(audioId: Int): Result<AudioContent>
    fun getBanners(): List<Banner>
    fun getHotRecommendations(): List<AudioContent>
    fun getNewReleases(): List<AudioContent>
}
