package com.ai.app.audio_ai.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.databinding.FragmentDetailBinding
import com.ai.app.audio_ai.ui.home.adapters.RecommendationAdapter
import com.ai.app.audio_ai.R
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import okhttp3.internal.concurrent.formatDuration

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by viewModels()
    private lateinit var relatedAdapter: RecommendationAdapter

    // 播放状态
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取传递的音频内容ID
        arguments?.getInt("audioId")?.let { audioId ->
            setupViews()
            setupObservers()
            viewModel.loadAudioContentDetail(audioId)
        } ?: run {
            // 如果没有传递ID，返回上一页
            findNavController().navigateUp()
            Toast.makeText(context, "参数错误", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews() {
        // 设置工具栏
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 设置重试按钮
        binding.btnRetry.setOnClickListener {
            arguments?.getInt("audioId")?.let { audioId ->
                binding.errorContainer.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                viewModel.loadAudioContentDetail(audioId)
            }
        }

        // 设置播放/暂停按钮
        binding.ivPlayPause.setOnClickListener {
            togglePlayPause()
        }

        // 设置进度条
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // TODO: 实现音频播放进度控制
                    updateCurrentTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                // 开始拖动时的处理
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // 停止拖动时的处理
            }
        })

        // 初始化相关推荐列表
        setupRelatedList()
    }

    private fun setupRelatedList() {
        relatedAdapter = RecommendationAdapter(
            items = emptyList(),
            onItemClick = { audioContent ->
                // 处理点击事件
                findNavController().navigate(
                    R.id.action_detailFragment_to_audioDetailFragment,
                    bundleOf("audioId" to audioContent.id.toInt())
                )
            }
        )
        binding.rvRelated.adapter = relatedAdapter
        binding.rvRelated.layoutManager = LinearLayoutManager(
            context, RecyclerView.HORIZONTAL, false
        )


    }

    private fun setupObservers() {
        // 观察音频内容详情数据
        viewModel.audioContent.observe(viewLifecycleOwner) { content ->
            updateContent(content)
        }

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.contentContainer.visibility = if (viewModel.audioContent.value != null) View.VISIBLE else View.GONE
            }
        }

        // 观察错误信息
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.tvErrorMessage.text = errorMessage
                binding.errorContainer.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
                viewModel.clearErrorMessage()
            } else {
                binding.errorContainer.visibility = View.GONE
            }
        }
    }

    private fun updateContent(content: AudioContent) {
        // 更新封面图
        Glide.with(this)
            .load(content.coverUrl)
            .centerCrop()
            .into(binding.ivCover)

        // 更新标题和作者
        binding.collapsingToolbar.title = content.title
        binding.tvTitle.text = content.title
        binding.tvAuthor.text = content.author

        // 更新播放统计信息
        val playCount = when {
            content.playCount >= 10000 -> String.format("%.1f万次播放", content.playCount / 10000.0)
            else -> "${content.playCount}次播放"
        }
        binding.tvPlayCount.text = playCount

        // 更新时长
        binding.tvDuration.text = formatDuration(content.duration)

        // 更新简介
        binding.tvDescription.text = content.description

        // 更新相关推荐
        val relatedList = try {
            // 使用反射获取推荐数据
            val recommendationsField = content::class.java.getDeclaredField("recommendations")
            recommendationsField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            recommendationsField.get(content) as? List<Any> ?: emptyList()
        } catch (e: Exception) {
            Log.e("DetailFragment", "Failed to get recommendations", e)
            emptyList()
        }
        @Suppress("UNCHECKED_CAST")
        relatedAdapter.items = relatedList as List<AudioContent>
        relatedAdapter.notifyDataSetChanged()

        // 显示内容
        binding.contentContainer.visibility = View.VISIBLE

        // 初始化播放器状态
        initializePlayer(content)
    }

    private fun initializePlayer(content: AudioContent) {
        // TODO: 实现音频播放器初始化
        binding.seekBar.max = content.duration.toInt()
        binding.seekBar.progress = 0
        updateCurrentTime(0)
        isPlaying = false
        updatePlayPauseButton()
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        // TODO: 实现音频播放/暂停功能
        updatePlayPauseButton()
        Toast.makeText(context, if (isPlaying) "开始播放" else "暂停播放", Toast.LENGTH_SHORT).show()
    }

    private fun updatePlayPauseButton() {
        binding.ivPlayPause.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun updateCurrentTime(progress: Int) {
        val duration = viewModel.audioContent.value?.duration ?: 0
        binding.tvCurrentTime.text = "${formatDuration(progress)} / ${formatDuration(duration)}"
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
