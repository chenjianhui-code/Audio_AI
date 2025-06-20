package com.ai.app.audio_ai.data.model

data class AudioContent(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val audioUrl: String,
    val duration: Long, // 单位：秒
    val category: String,
    val description: String? = null,
    val playCount: Int = 0,
    val likeCount: Int = 0
) {
    val formattedDuration: String
        get() {
            val minutes = duration / 60
            val seconds = duration % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
