package com.example.audio_ai.ui.robot

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.audio_ai.R
import com.ai.app.audio_ai.databinding.FragmentRobotBroadcastBinding
import java.util.*

private const val TAG = "RobotBroadcastFragment"

class RobotBroadcastFragment : Fragment() {

    private var _binding: FragmentRobotBroadcastBinding? = null
    private val binding get() = _binding!!

    private lateinit var ttsManager: TTSManager
    private var mouthAnimation: AnimationDrawable? = null
    private var isInitialized = false
    
    private val robotStyles = arrayOf("圆形机器人", "方形机器人")
    private val voiceStyles = arrayOf("默认声音", "男声", "女声", "儿童声")
    
    private var currentRobotStyle = 0
    private var currentVoiceStyle = 0
    
    // 播报队列
    private val broadcastQueue = LinkedList<String>()
    private var isPaused = false
    private var isPlaying = false
    private var currentBroadcastContent = "" // 当前正在播报的内容
    
    // 最近的播报历史
    private val broadcastHistory = mutableListOf<String>()
    private val MAX_HISTORY_SIZE = 10
    
    // 历史记录适配器
    private lateinit var historyAdapter: BroadcastHistoryAdapter
    
    // 音量控制
    private var volume = 0.8f
    
    // 进度条更新
    private val handler = Handler(Looper.getMainLooper())
    private var progressUpdateRunnable: Runnable? = null
    private var startTime: Long = 0
    private var totalDuration: Long = 0

    // TTS初始化状态
    private var isTTSInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRobotBroadcastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化TTS管理器
        initializeTTS()
        
        // 初始化历史记录列表
        setupHistoryRecyclerView()
        
        // 设置UI组件
        setupUIComponents()
        
        // 初始化机器人外观
        updateRobotAppearance()
    }

    private fun initializeTTS() {
        ttsManager = TTSManager(requireContext())
        ttsManager.initialize { success ->
            if (success) {
                isTTSInitialized = true
                Log.d(TAG, "TTS初始化成功")
                setupTTSListener()
                // 初始化成功后设置初始音量
                updateVolume()
            } else {
                Log.e(TAG, "TTS初始化失败")
                Toast.makeText(requireContext(), "语音引擎初始化失败，请检查系统设置", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupTTSListener() {
        ttsManager.setUtteranceListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                handler.post {
                    startTime = System.currentTimeMillis()
                    updatePlayingState(true)
                    startProgressUpdates()
                    Log.d(TAG, "开始播放：$utteranceId")
                }
            }

            override fun onDone(utteranceId: String?) {
                handler.post {
                    updatePlayingState(false)
                    stopProgressUpdates()
                    processNextInQueue()
                    Log.d(TAG, "播放完成：$utteranceId")
                }
            }

            override fun onError(utteranceId: String?) {
                handler.post {
                    updatePlayingState(false)
                    stopProgressUpdates()
                    Toast.makeText(requireContext(), "播放出错，请重试", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "播放错误：$utteranceId")
                }
            }
        })
    }

    private fun setupUIComponents() {
        // 设置播放相关按钮
        binding.startBroadcastButton.setOnClickListener {
            startBroadcast()
        }
        
        binding.addToQueueButton.setOnClickListener {
            addToQueue()
        }
        
        binding.pauseResumeButton.setOnClickListener {
            if (isPlaying) {
                pauseBroadcast()
            } else {
                resumeBroadcast()
            }
        }
        
        binding.stopButton.setOnClickListener {
            stopBroadcast()
        }
        
        // 设置机器人样式选择
        binding.robotStyleSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            robotStyles
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.robotStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentRobotStyle = position
                updateRobotAppearance()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 设置声音样式选择
        binding.voiceStyleSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            voiceStyles
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.voiceStyleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentVoiceStyle = position
                updateVoiceStyle()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 设置音量控制
        setupVolumeControl()
        
        // 初始化按钮状态
        updateButtonStates(false)
    }

    private fun updateButtonStates(playing: Boolean) {
        binding.startBroadcastButton.isEnabled = !playing
        binding.addToQueueButton.isEnabled = true
        binding.pauseResumeButton.isEnabled = playing
        binding.stopButton.isEnabled = playing
        
        binding.pauseResumeButton.text = if (playing && !isPaused) "暂停" else "继续"
    }

    private fun setupVolumeControl() {
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        binding.volumeSeekBar.max = maxVolume
        binding.volumeSeekBar.progress = (volume * maxVolume).toInt()
        
        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    volume = progress.toFloat() / maxVolume
                    updateVolume()
                    // 同步更新系统音量
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = BroadcastHistoryAdapter(broadcastHistory) { content ->
            binding.broadcastContentEditText.setText(content)
        }
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
        }
    }

    private fun startBroadcast() {
        if (!isTTSInitialized) {
            Toast.makeText(requireContext(), "语音引擎未就绪，请稍后重试", Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.broadcastContentEditText.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "请输入要播报的内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 停止当前播放
        if (isPlaying) {
            ttsManager.stop()
        }

        // 开始新的播报
        currentBroadcastContent = text
        startSpeaking(text)

        // 添加到历史记录
        addToHistory(text)
    }

    private fun addToQueue() {
        if (!isTTSInitialized) {
            Toast.makeText(requireContext(), "语音引擎未就绪，请稍后重试", Toast.LENGTH_SHORT).show()
            return
        }

        val text = binding.broadcastContentEditText.text.toString()
        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "请输入要加入队列的内容", Toast.LENGTH_SHORT).show()
            return
        }

        // 将内容加入队列
        broadcastQueue.offer(text)
        Toast.makeText(requireContext(), "已加入播报队列", Toast.LENGTH_SHORT).show()
        
        // 如果当前没有播放，则开始播放
        if (!isPlaying) {
            processNextInQueue()
        }
        
        // 添加到历史记录
        addToHistory(text)
    }

    private fun startSpeaking(text: String) {
        if (ttsManager.speak(text)) {
            updatePlayingState(true)
            startProgressUpdates()
            Log.d(TAG, "开始播报: $text")
        } else {
            Toast.makeText(requireContext(), "播放失败，请重试", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "播报失败: $text")
        }
    }

    private fun pauseBroadcast() {
        if (isPlaying && !isPaused) {
            ttsManager.pause()
            isPaused = true
            updateButtonStates(true)
            stopMouthAnimation()
            Log.d(TAG, "暂停播报")
        }
    }

    private fun resumeBroadcast() {
        if (isPlaying && isPaused) {
            ttsManager.resume()
            isPaused = false
            updateButtonStates(true)
            startMouthAnimation()
            Log.d(TAG, "继续播报")
        }
    }

    private fun stopBroadcast() {
        if (isPlaying) {
            ttsManager.stop()
            updatePlayingState(false)
            stopProgressUpdates()
            isPaused = false
            Log.d(TAG, "停止播报")
        }
    }

    private fun processNextInQueue() {
        if (broadcastQueue.isNotEmpty()) {
            val nextText = broadcastQueue.poll()
            nextText?.let {
                currentBroadcastContent = it
                startSpeaking(it)
            }
        }
    }

    private fun updatePlayingState(playing: Boolean) {
        isPlaying = playing
        updateButtonStates(playing)
        
        if (playing) {
            startMouthAnimation()
        } else {
            stopMouthAnimation()
            // 清空进度条
            binding.playbackProgressBar.progress = 0
            binding.progressPercentText.text = "0%"
        }
    }

    private fun startMouthAnimation() {
        // 设置机器人说话动画
        binding.mouthImageView.setImageResource(R.drawable.robot_speaking_animation)
        mouthAnimation = binding.mouthImageView.drawable as? AnimationDrawable
        mouthAnimation?.start()
    }

    private fun stopMouthAnimation() {
        mouthAnimation?.stop()
        // 恢复默认嘴型
        binding.mouthImageView.setImageResource(R.drawable.robot_mouth_default)
    }

    private fun updateRobotAppearance() {
        val bodyResourceId = when (currentRobotStyle) {
            0 -> R.drawable.robot_circle_body
            1 -> R.drawable.robot_square_body
            else -> R.drawable.robot_circle_body
        }
        binding.robotImageView.setImageResource(bodyResourceId)
        
        // 确保嘴部动画在样式切换时保持正确状态
        if (isPlaying && !isPaused) {
            startMouthAnimation()
        } else {
            stopMouthAnimation()
        }
    }

    private fun updateVoiceStyle() {
        // 这里可以根据不同的声音样式设置TTS的音色参数
        Log.d(TAG, "切换声音样式：${voiceStyles[currentVoiceStyle]}")
    }

    private fun updateVolume() {
        ttsManager.setVolume(volume)
    }

    private fun startProgressUpdates() {
        // 估算播报时长：假设每个字符需要0.2秒
        val textLength = currentBroadcastContent.length
        totalDuration = (textLength * 200).toLong().coerceAtLeast(1000) // 至少1秒
        startTime = System.currentTimeMillis()
        
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                if (isPlaying && !isPaused) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = ((elapsed.toFloat() / totalDuration) * 100).toInt().coerceIn(0, 100)
                    binding.playbackProgressBar.progress = progress
                    binding.progressPercentText.text = "${progress}%"
                    
                    if (progress < 100) {
                        handler.postDelayed(this, 100)
                    } else {
                        // 播放完成
                        processNextInQueue()
                    }
                }
            }
        }
        handler.post(progressUpdateRunnable!!)
    }

    private fun stopProgressUpdates() {
        progressUpdateRunnable?.let {
            handler.removeCallbacks(it)
        }
        binding.playbackProgressBar.progress = 0
        binding.progressPercentText.text = "0%"
    }

    private fun addToHistory(text: String) {
        if (broadcastHistory.size >= MAX_HISTORY_SIZE) {
            broadcastHistory.removeAt(broadcastHistory.size - 1)
        }
        broadcastHistory.add(0, text)
        historyAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ttsManager.release()
        _binding = null
    }

    // 历史记录适配器
    private inner class BroadcastHistoryAdapter(
        private val history: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<BroadcastHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val text = history[position]
            (holder.itemView as android.widget.TextView).text = text
            holder.itemView.setOnClickListener { onItemClick(text) }
        }

        override fun getItemCount() = history.size
    }
}
