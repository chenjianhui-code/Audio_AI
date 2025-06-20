package com.ai.app.audio_ai.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.data.repository.AudioRepository
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: AudioRepository
) : ViewModel() {

    // 音频内容详情数据
    private val _audioContent = MutableLiveData<AudioContent>()
    val audioContent: LiveData<AudioContent> = _audioContent

    // 加载状态
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 错误信息
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * 加载音频内容详情
     * @param id 音频内容ID
     */
    fun loadAudioContentDetail(id: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            repository.getAudioContent(id).fold(
                onSuccess = { content ->
                    _audioContent.value = content
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorMessage.value = "加载详情失败: ${error.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * 清除错误信息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
