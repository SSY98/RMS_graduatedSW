package com.project.rms.Shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.rms.R
import com.project.rms.Recipe.ssy_Recipe_Ladapter

class ssh_ShopAdapter (val itemList: ArrayList<ssh_Shop_item>): RecyclerView.Adapter<ssh_ShopAdapter.ViewHolder>() {
    // (1) 아이템 레이아웃과 결합
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_shop, parent, false)
        return ViewHolder(view)
    }
    // (2) 리스트 내 아이템 개수
    override fun getItemCount(): Int {
        return itemList.size
    }
    // (3) View에 내용 입력
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //제품 이미지
        holder.apply {
            Glide.with(holder.shopimg.context)
                .load(itemList[position].productimage)
                .into(holder.shopimg)
        }
        holder.product.text = itemList[position].productname //제품이름
        holder.shopname.text = itemList[position].storename //판매처 이름
        holder.price.text = itemList[position].lowprice //최저가
        //제품 클릭 이벤트(1)
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }
    //제품 클릭 이벤트(2)
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    //제품 클릭 이벤트(3)
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
    //제품 클릭 이벤트(4)
    private lateinit var itemClickListener : OnItemClickListener

    // (4) 레이아웃 내 View 연결
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val product: TextView = itemView.findViewById(R.id.shop_text) //제품 이름
        val shopimg: ImageView = itemView.findViewById(R.id.shop_img) //제품 이미지
        val shopname: TextView = itemView.findViewById(R.id.shop_store) //판매처
        val price: TextView = itemView.findViewById(R.id.shop_price) //최저가
    }
}