package com.ai.app.audio_ai.ui.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.audio_ai.R
import com.ai.app.audio_ai.data.model.Banner
import com.ai.app.audio_ai.utils.loadImage
import com.bumptech.glide.Glide

class BannerAdapter(
    private val banners: List<Banner>,
    private val onBannerClick: (Banner) -> Unit
) : RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        holder.bind(banner)
    }

    override fun getItemCount(): Int = banners.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBanner: ImageView = itemView.findViewById(R.id.iv_banner)

        fun bind(banner: Banner) {
            // 加载图片
            ivBanner.loadImage(banner.imageUrl)

            // 设置点击事件
            itemView.setOnClickListener {
                onBannerClick(banner)
            }
        }
    }
}
