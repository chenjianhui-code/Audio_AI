package com.ai.app.audio_ai.data.model

data class Banner(
    val id: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val targetUrl: String? = null
)
