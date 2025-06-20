package com.ai.app.audio_ai.ui.robot

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

private const val TAG = "TTSManager"

/**
 * TTS管理器
 * 负责管理文本转语音功能
 */
class TTSManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var utteranceListener: UtteranceProgressListener? = null
    private var volume: Float = 1.0f
    private var isInitialized = false

    /**
     * 初始化TTS引擎
     * @param onInitialized 初始化完成的回调，参数为是否初始化成功
     */
    fun initialize(onInitialized: (Boolean) -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            isInitialized = status == TextToSpeech.SUCCESS

            if (isInitialized) {
                // 设置语言
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "语言不支持")
                    isInitialized = false
                }
            }

            onInitialized(isInitialized)
        }
    }

    /**
     * 设置播报监听器
     */
    fun setUtteranceListener(listener: UtteranceProgressListener) {
        this.utteranceListener = listener
        textToSpeech?.setOnUtteranceProgressListener(listener)
    }

    /**
     * 设置音量
     * @param volume 音量值，范围0.0-1.0
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }

    /**
     * 开始播报
     * @param text 要播报的文本
     * @return 是否成功开始播报
     */
    fun speak(text: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "TTS未初始化")
            return false
        }

        val params = HashMap<String, String>().apply {
            put(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.toString())
            put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId")
        }

        val result = textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
        return result == TextToSpeech.SUCCESS
    }

    /**
     * 暂停播报
     */
    fun pause() {
        if (isInitialized && textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
    }

    /**
     * 继续播报
     */
    fun resume() {
        // Android TTS不直接支持恢复播放，需要重新开始
        // 这里可以通过在Fragment中保存当前播放内容来实现
    }

    /**
     * 停止播报
     */
    fun stop() {
        if (isInitialized) {
            textToSpeech?.stop()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        textToSpeech?.let { tts ->
            tts.stop()
            tts.shutdown()
            textToSpeech = null
        }
        utteranceListener = null
        isInitialized = false
    }
}
