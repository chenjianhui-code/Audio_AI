package com.ai.app.audio_ai.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.ai.app.audio_ai.R

class FloatingWindowService : Service() {

    private val TAG = "FloatingWindowService"
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var speechRecognitionService: SpeechRecognitionService? = null

    private var isRecording = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    // UI元素
    private var floatingIcon: View? = null
    private var recordingIndicator: ProgressBar? = null
    private var recordingText: TextView? = null

    // 动画
    private var scaleUpAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null

    override fun onCreate() {
        super.onCreate()
        initializeWindowManager()
        initializeFloatingWindow()
        initializeSpeechRecognition()
        initializeAnimations()
    }

    private fun initializeWindowManager() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun initializeFloatingWindow() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.layout_floating_window, null)

        // 获取UI元素引用
        floatingIcon = floatingView?.findViewById(R.id.floating_icon)
        recordingIndicator = floatingView?.findViewById(R.id.recording_indicator)
        recordingText = floatingView?.findViewById(R.id.recording_text)

        // 设置窗口参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // 初始位置
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100

        // 设置触摸监听器
        setupTouchListener()

        // 添加到窗口管理器
        windowManager?.addView(floatingView, params)
    }

    private fun initializeSpeechRecognition() {
        speechRecognitionService = SpeechRecognitionService(this)

        // 设置语音识别回调
        speechRecognitionService?.setOnResultCallback { result ->
            handleSpeechResult(result)
        }

        speechRecognitionService?.setOnReadyForSpeechCallback {
            showRecordingState(true)
        }

        speechRecognitionService?.setOnEndOfSpeechCallback {
            showRecordingState(false)
        }

        speechRecognitionService?.setOnErrorCallback { errorCode ->
            handleSpeechError(errorCode)
        }
    }

    private fun initializeAnimations() {
        scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        scaleDownAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_down)
    }

    private fun setupTouchListener() {
        floatingView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录初始位置
                    val params = view.layoutParams as WindowManager.LayoutParams
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    // 应用按下动画
                    floatingIcon?.startAnimation(scaleUpAnimation)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 应用释放动画
                    floatingIcon?.startAnimation(scaleDownAnimation)

                    // 检查是否是点击而不是拖动
                    val movedX = Math.abs(event.rawX - initialTouchX)
                    val movedY = Math.abs(event.rawY - initialTouchY)

                    if (movedX < 10 && movedY < 10) {
                        handleIconClick()
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 更新位置
                    val params = view.layoutParams as WindowManager.LayoutParams
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, params)
                    true
                }

                else -> false
            }
        }
    }

    private fun handleIconClick() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (!isRecording) {
            isRecording = true
            speechRecognitionService?.startListening()
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            isRecording = false
            speechRecognitionService?.stopListening()
            showRecordingState(false)
        }
    }

    private fun showRecordingState(isRecording: Boolean) {
        if (isRecording) {
            recordingIndicator?.visibility = View.VISIBLE
            recordingText?.visibility = View.VISIBLE
        } else {
            recordingIndicator?.visibility = View.GONE
            recordingText?.visibility = View.GONE
        }
    }

    private fun handleSpeechResult(result: String) {
        Log.d(TAG, "Speech result: $result")
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

        // 这里可以添加语音命令处理逻辑
        // 例如：解析命令并执行相应操作

        stopRecording()
    }

    private fun handleSpeechError(errorCode: Int) {
        Log.e(TAG, "Speech recognition error: $errorCode")
        Toast.makeText(this, "语音识别错误: $errorCode", Toast.LENGTH_SHORT).show()
        stopRecording()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
        speechRecognitionService?.destroy()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FloatingWindowService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingWindowService::class.java)
            context.stopService(intent)
        }
    }
}