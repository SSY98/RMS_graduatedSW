package com.project.rms.Foodlist

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.R
import java.util.*

class LinearListViewAdapter(var list : MutableList<ssh_ProductEntity>, var ssh_OnProductDeleteListner: ssh_OnProductDeleteListener) :
    RecyclerView.Adapter<LinearListViewAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemMoveListener  {

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.food_item_layout, parent, false)
    ) {
        val main_food_name: TextView = itemView.findViewById(R.id.main_food_name)
        val main_food_category: TextView = itemView.findViewById(R.id.main_food_category)
        val main_food_date: TextView = itemView.findViewById(R.id.main_food_date)
        val main_food_count: TextView = itemView.findViewById(R.id.main_food_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //position = 순서
        if(position<=1)
        {
            holder.itemView.setBackgroundColor(Color.RED)
        }else{holder.itemView.setBackgroundColor(Color.rgb(150,179,226))}

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ssh_FoodListActivity::class.java)
            startActivity(holder.itemView.context,intent,null)
        }

        val product = list[position]

        holder.main_food_name.text = product.name
        holder.main_food_category.text = product.category
        holder.main_food_date.text = product.date
        holder.main_food_count.text = product.count
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