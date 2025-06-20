package com.ai.app.audio_ai.service

import android.content.Context
import android.util.Log

class VoiceCommandProcessor(private val context: Context) {

    private val TAG = "VoiceCommandProcessor"
    private var commandCallback: ((CommandResult) -> Unit)? = null

    // 命令执行结果数据类
    data class CommandResult(
        val success: Boolean,
        val message: String,
        val command: String,
        val action: CommandAction? = null
    )

    // 命令动作枚举
    enum class CommandAction {
        START_BROADCAST,    // 开始播报
        STOP_BROADCAST,     // 停止播报
        ADJUST_VOLUME,      // 调整音量
        ADJUST_SPEED,       // 调整语速
        ADJUST_PITCH,       // 调整音调
        CLOSE_FLOATING     // 关闭浮动窗口
    }

    // 处理语音命令
    fun processCommand(command: String): CommandResult {
        Log.d(TAG, "Processing command: $command")

        // 将命令转换为小写并去除首尾空格，方便匹配
        val normalizedCommand = command.trim().lowercase()

        return when {
            // 开始播报相关命令
            normalizedCommand.contains("播报") || normalizedCommand.contains("朗读") -> {
                val text = extractTextToSpeak(normalizedCommand)
                if (text.isNotEmpty()) {
                    CommandResult(
                        success = true,
                        message = "开始播报: $text",
                        command = command,
                        action = CommandAction.START_BROADCAST
                    )
                } else {
                    CommandResult(
                        success = false,
                        message = "未能识别要播报的内容",
                        command = command
                    )
                }
            }

            // 停止播报命令
            normalizedCommand.contains("停止") || normalizedCommand.contains("暂停") -> {
                CommandResult(
                    success = true,
                    message = "停止播报",
                    command = command,
                    action = CommandAction.STOP_BROADCAST
                )
            }

            // 音量调整命令
            normalizedCommand.contains("音量") -> {
                val volume = extractNumericValue(normalizedCommand)
                if (volume != null) {
                    CommandResult(
                        success = true,
                        message = "调整音量至 $volume",
                        command = command,
                        action = CommandAction.ADJUST_VOLUME
                    )
                } else {
                    CommandResult(
                        success = false,
                        message = "未能识别音量值",
                        command = command
                    )
                }
            }

            // 语速调整命令
            normalizedCommand.contains("语速") || normalizedCommand.contains("速度") -> {
                val speed = extractNumericValue(normalizedCommand)
                if (speed != null) {
                    CommandResult(
                        success = true,
                        message = "调整语速至 $speed",
                        command = command,
                        action = CommandAction.ADJUST_SPEED
                    )
                } else {
                    CommandResult(
                        success = false,
                        message = "未能识别语速值",
                        command = command
                    )
                }
            }

            // 音调调整命令
            normalizedCommand.contains("音调") || normalizedCommand.contains("语调") -> {
                val pitch = extractNumericValue(normalizedCommand)
                if (pitch != null) {
                    CommandResult(
                        success = true,
                        message = "调整音调至 $pitch",
                        command = command,
                        action = CommandAction.ADJUST_PITCH
                    )
                } else {
                    CommandResult(
                        success = false,
                        message = "未能识别音调值",
                        command = command
                    )
                }
            }

            // 关闭浮动窗口命令
            normalizedCommand.contains("关闭") || normalizedCommand.contains("退出") -> {
                CommandResult(
                    success = true,
                    message = "关闭浮动窗口",
                    command = command,
                    action = CommandAction.CLOSE_FLOATING
                )
            }

            // 无法识别的命令
            else -> {
                CommandResult(
                    success = false,
                    message = "无法识别的命令",
                    command = command
                )
            }
        }
    }

    // 提取要播报的文本
    private fun extractTextToSpeak(command: String): String {
        // 移除命令词，保留要播报的内容
        val commandWords = listOf("播报", "朗读")
        var text = command
        for (word in commandWords) {
            text = text.replace(word, "")
        }
        return text.trim()
    }

    // 从命令中提取数值
    private fun extractNumericValue(command: String): Int? {
        val regex = Regex("\\d+")
        val match = regex.find(command)
        return match?.value?.toIntOrNull()
    }

    // 设置命令处理回调
    fun setCommandCallback(callback: (CommandResult) -> Unit) {
        this.commandCallback = callback
    }

    // 执行命令并通知回调
    fun executeCommand(command: String) {
        val result = processCommand(command)
        commandCallback?.invoke(result)
    }
}