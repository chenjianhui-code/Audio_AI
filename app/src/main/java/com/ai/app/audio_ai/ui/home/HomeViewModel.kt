package com.ai.app.audio_ai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.data.model.Banner
import com.ai.app.audio_ai.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {

    private val _hotRecommendations = MutableStateFlow<List<AudioContent>>(emptyList())
    val hotRecommendations: StateFlow<List<AudioContent>> = _hotRecommendations.asStateFlow()

    private val _newReleases = MutableStateFlow<List<AudioContent>>(emptyList())
    val newReleases: StateFlow<List<AudioContent>> = _newReleases.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _banners.value = repository.getBanners()
                _hotRecommendations.value = repository.getHotRecommendations()
                _newReleases.value = repository.getNewReleases()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
