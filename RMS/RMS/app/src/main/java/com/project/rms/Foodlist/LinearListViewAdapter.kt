package com.project.rms.Foodlist

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.R
import java.text.SimpleDateFormat
import java.util.*

class LinearListViewAdapter(var list : MutableList<ssh_ProductEntity>, var ssh_OnProductDeleteListner: ssh_OnProductDeleteListener) :
    RecyclerView.Adapter<LinearListViewAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemMoveListener  {

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.food_item_layout, parent, false)
    ) {
        val main_food_name: TextView = itemView.findViewById(R.id.main_food_name)
        val main_food_date: TextView = itemView.findViewById(R.id.main_food_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ssh_FoodListActivity::class.java)
            startActivity(holder.itemView.context,intent,null)
        }

        //position = 순서
        val product = list[position]

        val product_name = product.name
        val product_date = product.date

        holder.main_food_name.text = product_name
        holder.main_food_date.text = product_date

        var today = Calendar.getInstance() // 오늘 날짜
        var format = SimpleDateFormat("yyyy-M-d")
        var date = format.parse(product_date) // 유통기한 문자열을 날짜 형태로 변환
        var d_day = ((date.time - today.time.time) / (60 * 60 * 24 * 1000)) + 1 // 유통기한 날짜에서 오늘 날짜를 빼 남은 유통기한을 구함

        // 유통기한이 3일 이하 남은 식재료는 글자를 빨간색으로 나타냄
        if(d_day <= 3)
        {
            holder.main_food_name.setTextColor(Color.rgb(255,0,0))
            holder.main_food_date.setTextColor(Color.rgb(255,0,0))
        }else{
            holder.main_food_name.setTextColor(Color.rgb(0,0,0))
            holder.main_food_date.setTextColor(Color.rgb(0,0,0))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemSwiped(position: Int) {
        val product = list[position]
        list.removeAt(position)
        notifyItemRemoved(position)
        ssh_OnProductDeleteListner.onProductDeleteListener(product)
    }
}