package com.project.rms.Newfeed

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.project.rms.databinding.YsjItemAutoscrollContentBinding


class ExampleAdapter : ListAdapter<ysj_ExampleModel, ViewHolder>(ExampleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = YsjItemAutoscrollContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ysj_ExampleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemViewHolder = holder as ysj_ExampleViewHolder
        itemViewHolder.bind(getItem(position))

        holder.itemView.setOnClickListener {
//            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(link_1))
//            startActivity(holder.itemView.context,intent,null)
//            Log.d("clickTest", "아이템 클릭 확인. position : ${holder.adapterPosition}")
        }
    }

    fun onLinkItem(holder: ViewHolder, position: Int, onLinkItem: (String) -> Unit) {
        val itemViewHolder = holder as ysj_ExampleViewHolder
        itemViewHolder.onLinkItem(getItem(position), onLinkItem)
    }
}

class ExampleDiffCallback : DiffUtil.ItemCallback<ysj_ExampleModel>() {

    override fun areItemsTheSame(oldItem: ysj_ExampleModel, newItem: ysj_ExampleModel): Boolean =
        oldItem.message == newItem.message

    override fun areContentsTheSame(oldItem: ysj_ExampleModel, newItem: ysj_ExampleModel): Boolean =
        oldItem == newItem
}