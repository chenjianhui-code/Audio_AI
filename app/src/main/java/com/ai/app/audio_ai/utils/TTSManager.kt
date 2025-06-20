package com.ai.app.audio_ai.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TTSManager(context: Context) {
    private val TAG = "TTSManager"
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var onStartListener: (() -> Unit)? = null
    private var onDoneListener: (() -> Unit)? = null
    private var onErrorListener: (() -> Unit)? = null

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    isInitialized = true
                    setupProgressListener()
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    private fun setupProgressListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStartListener?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onDoneListener?.invoke()
            }

            override fun onError(utteranceId: String?) {
                onErrorListener?.invoke()
            }
        })
    }

    fun speak(text: String) {
        if (isInitialized) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        } else {
            Log.e(TAG, "TTS not initialized")
        }
    }

    fun setOnStartListener(listener: () -> Unit) {
        onStartListener = listener
    }

    fun setOnDoneListener(listener: () -> Unit) {
        onDoneListener = listener
    }

    fun setOnErrorListener(listener: () -> Unit) {
        onErrorListener = listener
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}