package com.project.rms.Memo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.App
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_item_food.view.*
import kotlinx.android.synthetic.main.ssh_item_memo.view.*

class ssh_MemoAdapter(var list : MutableList<ssh_MemoEntity>,
                      var ssh_onMemoDeleteListener : ssh_OnMemoDeleteListener,
                      var ssh_onMemoUpdateListner: ssh_OnMemoUpdateListener) :
    RecyclerView.Adapter<ssh_MemoAdapter.ViewHolder>() {

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_memo, parent, false)
    ) {
        val memo: TextView = itemView.findViewById(R.id.textview_memo)
        val root = itemView.MemoRoot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memo = list[position]
        val memoID = memo.id.toString()
        val memoContents = memo.memo
        holder.memo.text = memoContents

        // 메모를 길게 클릭
        holder.root.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                // SharedPreference에 선언한 변수에 클릭한 메모를 저장한다.
                App.prefs.MemoID = memoID
                App.prefs.MemoContents = memoContents
                ssh_onMemoUpdateListner.onMemoUpdateListner(memo) //  OnMemoUpdateListener 실행 (Listener는 수정 화면 dialog 창을 띄움)
                return true
            }
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}