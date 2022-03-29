package com.project.rms.Recipe.RecipeMake

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.rms.R

class ssy_RecipeMake_Ladapter (val itemList: ArrayList<ssy_RecipeMake_Litem>): RecyclerView.Adapter<ssy_RecipeMake_Ladapter.ViewHolder>() {
    // (1) 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ssy_RecipeMake_Ladapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ssy_litem_recipemake, parent, false)
        return ViewHolder(view)
    }
    // (2) 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return itemList.size
    }
    // (3) View에 내용 입력
    override fun onBindViewHolder(holder: ssy_RecipeMake_Ladapter.ViewHolder, position: Int) {
        holder.apply {
            Glide.with(holder.makeimgV.context)
                .load(itemList[position].makeimage)
                .into(holder.makeimgV)
        }
        holder.maketV.text = itemList[position].maketext
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // (4) 레이아웃 내 View 연결
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val maketV: TextView = itemView.findViewById(R.id.info_make)
        val makeimgV: ImageView = itemView.findViewById(R.id.info_img)
    }
}