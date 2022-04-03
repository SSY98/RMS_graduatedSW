package com.project.rms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.project.rms.Barcode.ssh_BarcodeCustom
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Recipe.ssy_RecipeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customdialogtest= findViewById<ImageButton>(R.id.setting)
        val StartRecognition = findViewById<Button>(R.id.BarcodeImageRecognition) // 바코드 이미지 인식 버튼_ssh
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

        // 바코드 이미지 인식 버튼 누르면 바코드 스캐너 화면을 출력함_ssh
        StartRecognition.setOnClickListener{
            var integrator = IntentIntegrator(this)
            integrator.setPrompt("바코드를 스캔하세요")
            integrator.setCameraId(0) // 0이면 후면 카메라, 1이면 전면 카메라
            integrator.captureActivity = ssh_BarcodeCustom::class.java // 커스텀한 바코드 화면
            integrator.initiateScan() // initiateScan()을 통해 Zxing 라이브러리 바코드 스캐너가 보여짐
        }

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
    // 바코드 스캔 결과 값을 받아 처리하는 곳
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "취소", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}