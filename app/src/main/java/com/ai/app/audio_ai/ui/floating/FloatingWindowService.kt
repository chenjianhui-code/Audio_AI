package com.ai.app.audio_ai.ui.floating

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.ai.app.audio_ai.R
import com.ai.app.audio_ai.service.SpeechRecognitionService
import com.ai.app.audio_ai.service.VoiceCommandProcessor

class FloatingWindowService : Service() {

    private val TAG = "FloatingWindowService"
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var speechRecognitionService: SpeechRecognitionService? = null
    private lateinit var voiceCommandProcessor: VoiceCommandProcessor
    private var commandListener: CommandListener? = null
    private val binder = LocalBinder()

    interface CommandListener {
        fun onCommandReceived(command: VoiceCommandProcessor.CommandResult)
    }

    fun setCommandListener(listener: CommandListener) {
        commandListener = listener
    }

    inner class LocalBinder : Binder() {
        fun getService(): FloatingWindowService = this@FloatingWindowService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private var isRecording = false
    private var isLongPressed = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable {
        isLongPressed = true
        showAudioWaveAnimation()
    }

    // UI元素
    private var floatingIcon: ImageView? = null
    private var recordingIndicator: ProgressBar? = null
    private var recordingText: TextView? = null

    // 动画
    private var scaleUpAnimation: Animation? = null
    private var scaleDownAnimation: Animation? = null

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "floating_window_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        initializeWindowManager()
        initializeFloatingWindow()
        initializeSpeechRecognition()
        initializeAnimations()
        initializeVoiceCommandProcessor()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Floating Window Service"
            val channelDescription = "Keeps the floating window active"
            val importance = android.app.NotificationManager.IMPORTANCE_LOW
            val channel = android.app.NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                importance
            ).apply {
                description = channelDescription
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = android.app.Notification.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setContentTitle("语音助手")
            setContentText("语音助手正在运行")
            setSmallIcon(R.drawable.ic_mic)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setForegroundServiceBehavior(android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE)
            }
        }.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14及以上版本需要指定前台服务类型
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun initializeVoiceCommandProcessor() {
        voiceCommandProcessor = VoiceCommandProcessor(this)
        voiceCommandProcessor.setCommandCallback { result ->
            // 在UI线程中处理命令结果
            Handler(Looper.getMainLooper()).post {
                when (result.action) {
                    VoiceCommandProcessor.CommandAction.CLOSE_FLOATING -> {
                        stopSelf()
                    }
                    else -> {
                        // 通知主界面处理其他命令
                        commandListener?.onCommandReceived(result)
                        // 显示命令处理结果
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
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
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "录音权限未授予")
            Toast.makeText(this, "录音权限未授予，语音识别功能将不可用", Toast.LENGTH_SHORT).show()
            return
        }

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

                    Log.d(TAG, "ACTION_DOWN - initialX: $initialX, initialY: $initialY, rawX: ${event.rawX}, rawY: ${event.rawY}")

                    // 应用按下动画
                    floatingIcon?.startAnimation(scaleUpAnimation)
                    
                    // 设置长按检测
                    longPressHandler.postDelayed(longPressRunnable, 500) // 500ms为长按阈值
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 取消长按检测
                    longPressHandler.removeCallbacks(longPressRunnable)
                    
                    // 如果是长按状态，则重置状态
                    if (isLongPressed) {
                        isLongPressed = false
                        hideAudioWaveAnimation()
                    } else {
                        // 应用释放动画
                        floatingIcon?.startAnimation(scaleDownAnimation)

                        // 检查是否是点击而不是拖动
                        val movedX = Math.abs(event.rawX - initialTouchX)
                        val movedY = Math.abs(event.rawY - initialTouchY)

                        if (movedX < 10 && movedY < 10) {
                            handleIconClick()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    // 如果移动距离超过阈值，取消长按检测
                    val movedX = Math.abs(event.rawX - initialTouchX)
                    val movedY = Math.abs(event.rawY - initialTouchY)
                    if (movedX > 10 || movedY > 10) {
                        longPressHandler.removeCallbacks(longPressRunnable)
                    }
                    
                    // 计算新位置
                    val newX = initialX + (event.rawX - initialTouchX).toInt()
                    val newY = initialY + (event.rawY - initialTouchY).toInt()
                    
                    Log.d(TAG, "ACTION_MOVE - newX: $newX, newY: $newY")

                    // 更新位置
                    val params = view.layoutParams as WindowManager.LayoutParams
                    params.x = newX
                    params.y = newY
                    
                    try {
                        windowManager?.updateViewLayout(view, params)
                        Log.d(TAG, "Window position updated successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to update window position", e)
                    }
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
    
    private fun showAudioWaveAnimation() {
        Log.d(TAG, "Showing audio wave animation")
        floatingIcon?.let { imageView ->
            Glide.with(this)
                .asGif()
                .load(R.mipmap.icons8_audio_wave)
                .into(imageView)
        }
    }
    
    private fun hideAudioWaveAnimation() {
        Log.d(TAG, "Hiding audio wave animation")
        floatingIcon?.setImageResource(R.drawable.ic_mic)
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
        voiceCommandProcessor.executeCommand(result)
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

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
        speechRecognitionService?.destroy()
    }
}