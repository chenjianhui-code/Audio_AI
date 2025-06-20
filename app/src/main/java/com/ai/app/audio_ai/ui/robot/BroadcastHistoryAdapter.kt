package com.ai.app.audio_ai.ui.robot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ai.app.audio_ai.R

/**
 * 播报历史记录适配器
 * 用于显示历史播报内容列表
 */
class BroadcastHistoryAdapter(
    private val historyItems: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<BroadcastHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.historyItemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_broadcast_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyItems[position]
        holder.textView.text = item

        // 设置点击事件，点击历史记录项时将内容填充到输入框
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = historyItems.size
}
