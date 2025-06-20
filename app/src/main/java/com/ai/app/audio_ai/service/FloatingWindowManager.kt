package com.ai.app.audio_ai.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.ai.app.audio_ai.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FloatingWindowManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isShowing = false
    private var commandListener: CommandListener? = null

    // 定义命令监听器接口
    interface CommandListener {
        fun onCommandReceived(command: String)
    }

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun show() {
        if (isShowing) return

        // 创建悬浮窗视图
        floatingView = LayoutInflater.from(context).inflate(R.layout.layout_floating_window, null)

        // 设置悬浮按钮点击事件
        val floatingIcon = floatingView?.findViewById<FloatingActionButton>(R.id.floating_icon)
        floatingIcon?.setOnClickListener {
            // 当用户点击悬浮按钮时，通知监听器
            commandListener?.onCommandReceived("开始录音")
        }

        // 设置WindowManager参数
        val params = WindowManager.LayoutParams().apply {
            // 设置窗口类型
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            // 设置窗口格式
            format = PixelFormat.TRANSLUCENT

            // 设置窗口标志
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

            // 设置窗口大小和位置
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }

        try {
            // 添加悬浮窗到窗口管理器
            windowManager?.addView(floatingView, params)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hide() {
        if (!isShowing) return

        try {
            windowManager?.removeView(floatingView)
            floatingView = null
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isWindowShowing(): Boolean = isShowing

    fun setCommandListener(listener: CommandListener) {
        this.commandListener = listener
    }

    fun release() {
        hide()
        commandListener = null
    }
}