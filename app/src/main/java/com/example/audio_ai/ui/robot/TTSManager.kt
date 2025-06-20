package com.example.audio_ai.ui.robot

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TTSManager(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var currentVolume = 1.0f
    private var onInitListener: ((Boolean) -> Unit)? = null
    private var utteranceListener: UtteranceProgressListener? = null
    
    // 用于暂停/恢复功能
    private var currentText: String = ""
    private var currentPosition: Int = 0
    private var isPaused: Boolean = false

    fun initialize(onInit: (Boolean) -> Unit) {
        onInitListener = onInit
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupTTS()
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
                isInitialized = false
                onInitListener?.invoke(false)
            }
        }
    }

    private fun setupTTS() {
        textToSpeech?.let { tts ->
            // 设置音频流类型
            tts.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            // 设置语言
            val result = tts.setLanguage(Locale.CHINESE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported or missing data")
                isInitialized = false
                onInitListener?.invoke(false)
                return
            }

            // 检查TTS引擎
            if (tts.engines.isEmpty()) {
                Log.e(TAG, "No TTS engines available")
                isInitialized = false
                onInitListener?.invoke(false)
                return
            }

            isInitialized = true
            onInitListener?.invoke(true)
            Log.d(TAG, "TTS initialized successfully")
        }
    }

    fun setUtteranceListener(listener: UtteranceProgressListener) {
        utteranceListener = listener
        textToSpeech?.setOnUtteranceProgressListener(listener)
    }

    fun speak(text: String): Boolean {
        if (!isInitialized || textToSpeech == null) {
            Log.e(TAG, "TTS not initialized")
            return false
        }

        // 保存当前文本，用于暂停/恢复功能
        currentText = text
        currentPosition = 0
        isPaused = false

        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_VOLUME] = currentVolume.toString()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utteranceId"

        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
        return result != TextToSpeech.ERROR
    }

    fun pause() {
        if (isInitialized && textToSpeech?.isSpeaking == true) {
            // 暂停播放
            isPaused = true
            // 由于TextToSpeech没有原生的暂停功能，我们只能停止
            textToSpeech?.stop()
            // 理想情况下，我们应该记录当前播放位置，但TextToSpeech不提供这个功能
            // 这里我们简单地将位置设为文本长度的一半，作为近似
            currentPosition = currentText.length / 2
            Log.d(TAG, "TTS paused at approximate position: $currentPosition")
        }
    }

    fun resume() {
        if (isInitialized && isPaused) {
            // 尝试从近似位置恢复播放
            val remainingText = if (currentPosition < currentText.length) {
                currentText.substring(currentPosition)
            } else {
                currentText
            }
            
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_VOLUME] = currentVolume.toString()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utteranceId"
            
            textToSpeech?.speak(remainingText, TextToSpeech.QUEUE_FLUSH, params)
            isPaused = false
            Log.d(TAG, "TTS resumed from approximate position: $currentPosition")
        }
    }

    fun stop() {
        textToSpeech?.stop()
        isPaused = false
        currentPosition = 0
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    fun isSpeaking(): Boolean = textToSpeech?.isSpeaking ?: false

    companion object {
        private const val TAG = "TTSManager"
    }
}
