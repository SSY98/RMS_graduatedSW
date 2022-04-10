package com.project.rms.Foodlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ssh_FoodListActivity : AppCompatActivity(), ssh_OnProductDeleteListener {
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var productList = mutableListOf<ssh_ProductEntity>() // 식재료 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ssh_activity_food_list)

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        getAllProduct() // 데이터베이스에 있는 식재료를 불러옴_ssh
    }

    // 데이터베이스에 있는 식재료를 불러옴_ssh
    fun getAllProduct(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                productList = db.productDAO().getAll()
                Log.d("product","$productList")
            }.await()
            CoroutineScope(Dispatchers.Main).launch {
                setRecyclerView(productList)
            }
        }
    }

    // 데이터베이스에 있는 식재료를 수정_ssh
    fun updateProduct(product: ssh_ProductEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.productDAO().update(product)
                Log.d("memo", "hi5")
            }.await()
            getAllProduct()
            Log.d("memo", "hi6")
        }
    }

    // 데이터베이스에 있는 식재료를 삭제_ssh
    fun deleteProduct(product: ssh_ProductEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.productDAO().delete(product)
            }.await()
            getAllProduct()
        }
    }

    // recyclerview로 데이터베이스에 있는 식재료 출력_ssh
    fun setRecyclerView(productList : MutableList<ssh_ProductEntity>){
        val recyclerView = findViewById<RecyclerView>(R.id.FoodList_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ssh_FoodListAdapter(productList, this)//수정
        val callback = ItemTouchHelperCallback(adapter,this)//++
        val touchHelper = ItemTouchHelper(callback)//++
        touchHelper.attachToRecyclerView(recyclerView)//++
        recyclerView.adapter = adapter//++
    }

    override fun onProductDeleteListener(product: ssh_ProductEntity) {
        deleteProduct(product)
    }
}