package com.project.rms.Newfeed

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.project.rms.databinding.YsjItemAutoscrollContentBinding


class ysj_ExampleViewHolder(
    private val binding: ViewBinding
) : ViewHolder(binding.root) {

    fun bind(item: ysj_ExampleModel) {
        val binding = (binding as YsjItemAutoscrollContentBinding)

        binding.tvMessage.text = item.message
    }

    fun onLinkItem(item: ysj_ExampleModel, onLinkItem: (String) -> Unit) {
        onLinkItem.invoke(item.message)
    }
}