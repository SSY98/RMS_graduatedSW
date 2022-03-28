package com.project.rms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customdialogtest= findViewById<ImageButton>(R.id.setting)


        //리스트 뷰
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val list = mutableListOf("category1", "category2", "category3", "category4", "category5","cat","cat","cat","cat","cat","cat","cat","cat","cat","cat")

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



        // 설정칸으로 dialog(POPUP 창) 테스트함
        customdialogtest.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }


    }
}