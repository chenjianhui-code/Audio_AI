package com.ai.app.audio_ai.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.*

class SpeechRecognitionService(private val context: Context) {

    private val TAG = "SpeechRecognitionService"
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionListener: RecognitionListener? = null
    private var onResultCallback: ((String) -> Unit)? = null
    private var onReadyForSpeechCallback: (() -> Unit)? = null
    private var onEndOfSpeechCallback: (() -> Unit)? = null
    private var onErrorCallback: ((Int) -> Unit)? = null

    init {
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupRecognitionListener()
        } else {
            Log.e(TAG, "Speech recognition is not available on this device")
        }
    }

    private fun setupRecognitionListener() {
        recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
                onReadyForSpeechCallback?.invoke()
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 可以用来更新UI显示音量变化
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d(TAG, "onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
                onEndOfSpeechCallback?.invoke()
            }

            override fun onError(error: Int) {
                Log.e(TAG, "onError: $error")
                onErrorCallback?.invoke(error)
            }

            override fun onResults(results: Bundle?) {
                Log.d(TAG, "onResults")
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { matches ->
                    if (matches.isNotEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Recognized text: $recognizedText")
                        onResultCallback?.invoke(recognizedText)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d(TAG, "onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "onEvent: $eventType")
            }
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    fun startListening(language: String = Locale.getDefault().language) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun setOnResultCallback(callback: (String) -> Unit) {
        this.onResultCallback = callback
    }

    fun setOnReadyForSpeechCallback(callback: () -> Unit) {
        this.onReadyForSpeechCallback = callback
    }

    fun setOnEndOfSpeechCallback(callback: () -> Unit) {
        this.onEndOfSpeechCallback = callback
    }

    fun setOnErrorCallback(callback: (Int) -> Unit) {
        this.onErrorCallback = callback
    }
}