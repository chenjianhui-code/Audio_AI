package com.ai.app.audio_ai.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.ai.app.audio_ai.data.model.AudioContent

class AudioPlayerService : Service() {

    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudio: AudioContent? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun playAudio(audio: AudioContent) {
        currentAudio = audio
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audio.audioUrl)
            prepareAsync()
            setOnPreparedListener { start() }
            setOnCompletionListener { notifyPlaybackComplete() }
        }
    }

    fun pauseAudio() {
        mediaPlayer?.pause()
    }

    fun resumeAudio() {
        mediaPlayer?.start()
    }

    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentAudio(): AudioContent? = currentAudio

    private fun notifyPlaybackComplete() {
        // TODO: 通知播放完成
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
