package com.ai.app.audio_ai.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.service.AudioPlayerService

class AudioPlayerManager(private val context: Context) {

    private var playerService: AudioPlayerService? = null
    private var isBound = false
    private var currentAudio: AudioContent? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            playerService = binder.getService()
            isBound = true
            currentAudio?.let { play(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            playerService = null
        }
    }

    fun play(audio: AudioContent) {
        currentAudio = audio
        if (isBound) {
            playerService?.playAudio(audio)
        } else {
            val intent = Intent(context, AudioPlayerService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun pause() {
        if (isBound) {
            playerService?.pauseAudio()
        }
    }

    fun resume() {
        if (isBound) {
            playerService?.resumeAudio()
        }
    }

    fun stop() {
        if (isBound) {
            playerService?.stopAudio()
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    fun isPlaying(): Boolean {
        return if (isBound) {
            playerService?.isPlaying() ?: false
        } else false
    }

    fun getCurrentAudio(): AudioContent? {
        return if (isBound) {
            playerService?.getCurrentAudio()
        } else null
    }

    fun release() {
        stop()
        playerService = null
    }
}
