package com.project.rms.Foodlist

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.R
import com.project.rms.SwipeActivity
import java.util.*

class LinearListViewAdapter(private val list: MutableList<String>) :
    RecyclerView.Adapter<LinearListViewAdapter.ViewHolder>(),
    ItemTouchHelperCallback.OnItemMoveListener {

    private lateinit var dragListener: OnStartDragListener

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.food_item_layout, parent, false)
    ) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val ivMenu: ImageView = itemView.findViewById(R.id.imageView)
        //val inrecycler: Button = itemView.findViewById(R.id.inrecycler)
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

        list[position].let {
            with(holder) {
                tvTitle.text = it
                ivMenu.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        dragListener.onStartDrag(this)
                    }
                    return@setOnTouchListener false
                }
            }
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, SwipeActivity::class.java)
            startActivity(holder.itemView.context,intent,null)
        }

    }



    override fun getItemCount(): Int {
        return list.size
    }

    interface OnStartDragListener {
        fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    }

    fun startDrag(listener: OnStartDragListener) {
        this.dragListener = listener
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemSwiped(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

}