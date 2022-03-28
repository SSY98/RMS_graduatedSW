package com.project.rms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter

class SwipeActivity : AppCompatActivity(){
    override fun onCreate(savedInstance: Bundle?){
        super.onCreate(savedInstance)
        setContentView(R.layout.swipe_activity_recycler)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val list = mutableListOf("category1", "category2", "category3", "category4", "category5","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog","dog")

        val adapter = LinearListViewAdapter(list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val callback = ItemTouchHelperCallback(adapter,this)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter
        adapter.startDrag(object : LinearListViewAdapter.OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        })
    }
}