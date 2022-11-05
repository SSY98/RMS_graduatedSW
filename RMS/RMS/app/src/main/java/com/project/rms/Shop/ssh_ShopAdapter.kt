package com.project.rms.Shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.rms.R

class ssh_ShopAdapter (val itemList: ArrayList<ssh_Shop_item>): RecyclerView.Adapter<ssh_ShopAdapter.ViewHolder>() {
    // (1) 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ssh_ShopAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_shop, parent, false)
        return ViewHolder(view)
    }
    // (2) 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return itemList.size
    }
    // (3) View에 내용 입력
    override fun onBindViewHolder(holder: ssh_ShopAdapter.ViewHolder, position: Int) {
        holder.apply {
            Glide.with(holder.shopimg.context)
                .load(itemList[position].shopimage)
                .into(holder.shopimg)
        }
        holder.shoptext.text = itemList[position].shoptext
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // (4) 레이아웃 내 View 연결
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val shoptext: TextView = itemView.findViewById(R.id.shop_text)
        val shopimg: ImageView = itemView.findViewById(R.id.shop_img)
    }
}