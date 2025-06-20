package com.ai.app.audio_ai.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.ai.app.audio_ai.databinding.FragmentHomeBinding
import com.ai.app.audio_ai.ui.home.adapters.RecommendationAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val hotRecommendationAdapter by lazy {
        RecommendationAdapter(emptyList()) { item ->
            // Handle item click
        }
    }
    
    private val newReleaseAdapter by lazy {
        RecommendationAdapter(emptyList()) { item ->
            // Handle item click
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupObservers()
        viewModel.loadData()
    }

    private fun setupAdapters() {
        binding.rvHotRecommendations.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hotRecommendationAdapter
        }

        binding.rvNewReleases.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = newReleaseAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.hotRecommendations.collect { items ->
                        hotRecommendationAdapter.items = items
                        hotRecommendationAdapter.notifyDataSetChanged()
                    }
                }
                
                launch {
                    viewModel.newReleases.collect { items ->
                        newReleaseAdapter.items = items
                        newReleaseAdapter.notifyDataSetChanged()
                    }
                }
                
                launch {
                    viewModel.banners.collect { banners ->
                        // Setup banner view
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
