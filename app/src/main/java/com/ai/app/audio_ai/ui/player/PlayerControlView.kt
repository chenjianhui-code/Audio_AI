package com.ai.app.audio_ai.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.databinding.ViewPlayerControlBinding
import com.bumptech.glide.Glide

class PlayerControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewPlayerControlBinding = ViewPlayerControlBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    var onPlayPauseClick: (() -> Unit)? = null
    var onCloseClick: (() -> Unit)? = null

    init {
        binding.btnPlayPause.setOnClickListener { onPlayPauseClick?.invoke() }
        binding.btnClose.setOnClickListener { onCloseClick?.invoke() }
    }

    fun setAudio(audio: AudioContent) {
        binding.tvTitle.text = audio.title
        binding.tvAuthor.text = audio.author
        Glide.with(context)
            .load(audio.coverUrl)
            .into(binding.ivCover)
    }

    fun setPlaying(isPlaying: Boolean) {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) com.ai.app.audio_ai.R.drawable.ic_pause
            else com.ai.app.audio_ai.R.drawable.ic_play
        )
    }

    fun setProgress(progress: Int, duration: Int) {
        binding.progressBar.max = duration
        binding.progressBar.progress = progress
        binding.tvCurrentTime.text = formatTime(progress)
        binding.tvDuration.text = formatTime(duration)
    }

    fun show() {
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    private fun formatTime(millis: Int): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
