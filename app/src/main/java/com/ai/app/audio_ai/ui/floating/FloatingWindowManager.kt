package com.ai.app.audio_ai.ui.floating

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 浮动窗口管理器
 * 负责管理浮动窗口的显示和隐藏，以及处理权限检查和请求
 */
class FloatingWindowManager(private val context: Context) {

    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1234
        private const val RECORD_AUDIO_PERMISSION_CODE = 1235
    }

    private var commandListener: FloatingWindowService.CommandListener? = null
    private var isWindowShown = false

    /**
     * 切换浮动窗口状态
     * @param isActive 是否激活浮动窗口
     */
    fun toggleFloatingWindow(isActive: Boolean) {
        if (isActive) {
            show()
        } else {
            hide()
        }
    }

    /**
     * 设置命令监听器
     * @param listener 命令监听器
     */
    fun setCommandListener(listener: FloatingWindowService.CommandListener) {
        this.commandListener = listener
        // 如果窗口已经显示，则更新监听器
        if (isWindowShown && floatingWindowService != null) {
            floatingWindowService?.setCommandListener(listener)
        }
    }

    /**
     * 显示浮动窗口
     */
    fun show() {
        if (!isWindowShown) {
            startFloatingWindow()
            isWindowShown = true
        }
    }

    /**
     * 隐藏浮动窗口
     */
    fun hide() {
        if (isWindowShown) {
            stopFloatingWindow()
            isWindowShown = false
        }
    }

    /**
     * 启动浮动窗口
     */
    private var serviceConnection: ServiceConnection? = null
    private var floatingWindowService: FloatingWindowService? = null

    fun startFloatingWindow() {
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
            return
        }

        // 检查录音权限
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_CODE
                )
                Toast.makeText(context, "请授予录音权限以启动服务", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "需要录音权限才能启动服务", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val intent = Intent(context, FloatingWindowService::class.java)
            
            // 创建服务连接
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    // 获取服务实例并设置监听器
                    if (service is FloatingWindowService.LocalBinder) {
                        floatingWindowService = service.getService()
                        commandListener?.let {
                            floatingWindowService?.setCommandListener(it)
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    floatingWindowService = null
                }
            }
            
            try {
                // 先启动服务
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                
                // 等待一小段时间确保服务已启动
                Thread.sleep(100)
                
                // 然后绑定服务
                context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "启动服务失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * 停止浮动窗口
     */
    private fun stopFloatingWindow() {
        // 解绑服务
        serviceConnection?.let {
            context.unbindService(it)
            serviceConnection = null
        }
        // 停止服务
        val intent = Intent(context, FloatingWindowService::class.java)
        context.stopService(intent)
        floatingWindowService = null
    }

    /**
     * 检查是否有悬浮窗权限
     * @return 是否有权限
     */
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * 请求悬浮窗权限
     */
    private fun requestOverlayPermission() {
        if (context is Activity) {
            AlertDialog.Builder(context)
                .setTitle("需要悬浮窗权限")
                .setMessage("语音助手需要悬浮窗权限才能显示浮动按钮。请在接下来的设置页面中授予权限。")
                .setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
                }
                .setNegativeButton("取消") { _, _ ->
                    Toast.makeText(context, "未授予悬浮窗权限，无法显示浮动语音助手", Toast.LENGTH_SHORT).show()
                }
                .setCancelable(false)
                .show()
        } else {
            Toast.makeText(context, "请在设置中授予悬浮窗权限", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 处理权限请求结果
     * 在Activity的onActivityResult和onRequestPermissionsResult中调用此方法
     */
    fun handlePermissionResult(requestCode: Int, grantResults: IntArray = IntArray(0)) {
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (checkOverlayPermission()) {
                    startFloatingWindow()
                } else {
                    Toast.makeText(context, "未授予悬浮窗权限，无法显示浮动语音助手", Toast.LENGTH_SHORT).show()
                }
            }
            RECORD_AUDIO_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFloatingWindow()
                } else {
                    Toast.makeText(context, "未授予录音权限，无法启动语音助手服务", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    /**
     * 在Activity销毁时调用此方法，清理资源
     */
    fun onDestroy() {
        hide()
    }
}
