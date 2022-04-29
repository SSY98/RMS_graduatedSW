package com.project.rms.Recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.R

class ssy_Parts_Ladapter (val itemList: ArrayList<ssy_Parts_Litem>): RecyclerView.Adapter<ssy_Parts_Ladapter.ViewHolder>(){
    // (1) 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ssy_Parts_Ladapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ssy_litem_parts, parent, false)
        return ViewHolder(view)
    }
    // (2) 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return itemList.size
    }
    // (3) View에 내용 입력
    override fun onBindViewHolder(holder: ssy_Parts_Ladapter.ViewHolder, position: Int) {
        holder.nameV.text = itemList[position].parts
        // 2-(1) 클릭이벤트 설정
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // 2-(2) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    // 2-(3) setItemClickListener로 설정한 함수 실행
    private lateinit var itemClickListener : OnItemClickListener

    // (4) 레이아웃 내 View 연결
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameV: TextView = itemView.findViewById(R.id.name)
    }
}