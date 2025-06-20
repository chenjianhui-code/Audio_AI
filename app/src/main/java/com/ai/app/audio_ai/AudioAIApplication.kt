package com.ai.app.audio_ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AudioAIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 在这里初始化全局配置
        initGlide()
        initCrashReporting()
    }

    private fun initGlide() {
        // 初始化图片加载库
    }

    private fun initCrashReporting() {
        // 初始化崩溃报告
    }
}
