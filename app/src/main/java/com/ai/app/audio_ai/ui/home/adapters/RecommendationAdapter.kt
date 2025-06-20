package com.ai.app.audio_ai.ui.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.audio_ai.data.model.AudioContent
import com.ai.app.audio_ai.databinding.ItemRecommendationBinding
import com.bumptech.glide.Glide

class RecommendationAdapter(
    var items: List<AudioContent>,
    private val onItemClick: (AudioContent) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AudioContent) {
            binding.tvTitle.text = item.title
            binding.tvAuthor.text = item.author
            binding.tvDuration.text = item.formattedDuration

            Glide.with(binding.root.context)
                .load(item.coverUrl)
                .into(binding.ivCover)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
