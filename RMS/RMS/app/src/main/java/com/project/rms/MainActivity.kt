package com.project.rms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Recipe.ssy_RecipeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customdialogtest= findViewById<ImageButton>(R.id.setting)
        val recipeB= findViewById<Button>(R.id.recipe) //ssy


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

        //레시피 추천 버튼 누르면 들어가짐_ssy
        recipeB.setOnClickListener{
            val intent = Intent(this, ssy_RecipeActivity::class.java)
            startActivity(intent)
        }


        // 설정칸으로 dialog(POPUP 창) 테스트함
        customdialogtest.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }


    }
}