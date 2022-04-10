package com.project.rms.Foodlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_OnProductUpdateListener
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_item_food.view.*
import java.util.*

class ssh_FoodListAdapter(var list : MutableList<ssh_ProductEntity>,
                          var ssh_OnProductDeleteListener: ssh_OnProductDeleteListener) :
    RecyclerView.Adapter<ssh_FoodListAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemMoveListener  {

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.ssh_item_food, parent, false)
    ) {
        val activity_food_name: TextView = itemView.findViewById(R.id.activity_food_name)
        val activity_food_category: TextView = itemView.findViewById(R.id.activity_food_category)
        val activity_food_date: TextView = itemView.findViewById(R.id.activity_food_date)
        val activity_food_count: TextView = itemView.findViewById(R.id.activity_food_count)
        val activity_root = itemView.activity_rootView
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

        val product = list[position]

        holder.activity_food_name.text = product.name
        holder.activity_food_category.text = product.category
        holder.activity_food_date.text = product.date
        holder.activity_food_count.text = product.count
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
        ssh_OnProductDeleteListener.onProductDeleteListener(product)
    }
}