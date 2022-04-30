package com.project.rms.Memo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_item_memo.view.*

class ssh_MemoAdapter(var list : MutableList<ssh_MemoEntity>,
                      var ssh_onMemoDeleteListener : ssh_OnMemoDeleteListener) : RecyclerView.Adapter<ssh_MemoAdapter.MemoViewHolder>() {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_memo,parent,false)
        return MemoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val memo = list[position]
        val memoID = memo.id.toString()
        val memoContents = memo.memo
        holder.memo.text = memoContents
    }

    inner class MemoViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val memo: TextView = itemView.findViewById(R.id.textview_memo)
        val root = itemView.MemoRoot
    }
}