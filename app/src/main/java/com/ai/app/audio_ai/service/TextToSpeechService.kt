package com.ai.app.audio_ai.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TextToSpeechService(context: Context) {

    private val TAG = "TextToSpeechService"
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var currentVolume = 1.0f  // 默认音量 (0.0-1.0)
    private var currentSpeechRate = 1.0f  // 默认语速 (0.5-2.0)
    private var currentPitch = 1.0f  // 默认音调 (0.5-2.0)
    private var onInitListener: (() -> Unit)? = null
    private var onSpeakCompleteListener: (() -> Unit)? = null

    init {
        initTextToSpeech(context)
    }

    private fun initTextToSpeech(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "语言不支持")
                } else {
                    isInitialized = true
                    setupUtteranceProgressListener()
                    onInitListener?.invoke()
                    Log.d(TAG, "TextToSpeech 初始化成功")
                }
            } else {
                Log.e(TAG, "TextToSpeech 初始化失败: $status")
            }
        }
    }

    private fun setupUtteranceProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "开始播报: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "播报完成: $utteranceId")
                onSpeakCompleteListener?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "播报错误: $utteranceId")
            }
        })
    }

    fun speak(text: String): Boolean {
        if (!isInitialized || textToSpeech == null) {
            Log.e(TAG, "TextToSpeech 未初始化")
            return false
        }

        // 停止当前正在播报的内容
        stop()

        // 设置语速和音调
        textToSpeech?.setSpeechRate(currentSpeechRate)
        textToSpeech?.setPitch(currentPitch)

        // 播报文本
        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId_${System.currentTimeMillis()}")
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
            Log.d(TAG, "停止播报")
        }
    }

    fun setVolume(volume: Int) {
        // 将0-100的音量值转换为0.0-1.0
        currentVolume = (volume.coerceIn(0, 100) / 100.0f)
        Log.d(TAG, "设置音量: $currentVolume")
    }

    fun setSpeechRate(rate: Int) {
        // 将0-100的语速值转换为0.5-2.0
        currentSpeechRate = (0.5f + (rate.coerceIn(0, 100) / 100.0f) * 1.5f)
        Log.d(TAG, "设置语速: $currentSpeechRate")
    }

    fun setPitch(pitch: Int) {
        // 将0-100的音调值转换为0.5-2.0
        currentPitch = (0.5f + (pitch.coerceIn(0, 100) / 100.0f) * 1.5f)
        Log.d(TAG, "设置音调: $currentPitch")
    }

    fun setOnInitListener(listener: () -> Unit) {
        this.onInitListener = listener
        // 如果已经初始化，直接调用监听器
        if (isInitialized) {
            listener.invoke()
        }
    }

    fun setOnSpeakCompleteListener(listener: () -> Unit) {
        this.onSpeakCompleteListener = listener
    }

    fun isInitialized(): Boolean {
        return isInitialized
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        Log.d(TAG, "TextToSpeech 资源已释放")
    }
}