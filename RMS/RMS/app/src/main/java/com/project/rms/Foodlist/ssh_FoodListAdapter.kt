package com.project.rms.Foodlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_OnProductUpdateListener
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.R
import kotlinx.android.synthetic.main.ssh_item_food.view.*
import java.util.*

class ssh_FoodListAdapter(var list : MutableList<ssh_ProductEntity>,
                          var ssh_OnProductDeleteListener: ssh_OnProductDeleteListener,
                          var ssh_OnProductUpdateListener: ssh_OnProductUpdateListener) :
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
        val productID = product.id.toString()
        val product_name = product.name
        val product_category = product.category
        val product_date = product.date
        val product_count = product.count

        holder.activity_food_name.text = product_name
        holder.activity_food_category.text = product_category
        holder.activity_food_date.text = product_date
        holder.activity_food_count.text = product_count

        // 식재료 목록을 길게 클릭
        holder.activity_root.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                // SharedPreference에 선언한 변수에 클릭한 식재료의 이름, 종류, 유통기한, 갯수를 저장한다.
                App.prefs.FoodID = productID
                App.prefs.FoodName = product_name
                App.prefs.FoodCategory = product_category
                App.prefs.FoodDate = product_date
                App.prefs.FoodCount = product_count

                ssh_OnProductUpdateListener.onProductUpdateListener(product) //  OnProductUpdateListener 실행 (Listener는 수정 화면 dialog 창을 띄움)
                return true
            }
        })
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