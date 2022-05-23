package com.project.rms.Foodlist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.App
import com.project.rms.CountdownTimer.ssy_Countdowntimer
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_OnProductUpdateListener
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.UpdateDialog.ssh_FoodListUpdateDialog
import com.project.rms.Foodlist.UpdateDialog.ssh_FoodListUpdateDialogInterface
import com.project.rms.MainActivity
import com.project.rms.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ssh_FoodListActivity : AppCompatActivity(), ssh_FoodListUpdateDialogInterface, ssh_OnProductDeleteListener, ssh_OnProductUpdateListener {
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
        val adapter = ssh_FoodListAdapter(productList, this, this)//수정
        val callback = ItemTouchHelperCallback(adapter,this)//++
        val touchHelper = ItemTouchHelper(callback)//++
        touchHelper.attachToRecyclerView(recyclerView)//++
        recyclerView.adapter = adapter//++
    }

    // 스와이프 모션을 사용하면 데이터베이스 내의 식재료가 삭제됨
    override fun onProductDeleteListener(product: ssh_ProductEntity) {
        deleteProduct(product)
    }

    // 식재료 수정 화면 dialog를 출력
    override fun onProductUpdateListener(product: ssh_ProductEntity) {
        val UpdateDialog = ssh_FoodListUpdateDialog(this,this)
        UpdateDialog.show()
    }

    // 식재료 수정 팝업창 수정 버튼 클릭
    override fun onUpdateButtonClicked() {
        // 식재료 정보 수정 사항을 각각의 변수에 저장
        val currentID = App.prefs.FoodID?.toLong()
        var productname = App.prefs.FoodName.toString()
        var productcatergory = App.prefs.FoodCategory.toString()
        var productdate = App.prefs.FoodDate.toString()
        var productcount = App.prefs.FoodCount.toString()

        // 각각의 변수에 저장된 수정 사항을 데이터베이스에 적용
        val product = ssh_ProductEntity(currentID, productname, productcatergory, productdate, productcount)
        updateProduct(product)

        // SharedPreference 변수에 저장된 식재료 이름, 종류, 유통기한, 갯수 초기화 (= 식재료 추가, 수정 시 사용하는 edittext 초기화)
        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
        App.prefs.FoodCount = "1"

        val intent = Intent(this, ssh_FoodListActivity::class.java)
        startActivity(intent)
    }

    // 식재료 수정 팝업창에서 취소 버튼 클릭 시 SharedPreference 변수에 저장된 식재료 이름, 종류, 유통기한, 갯수 초기화 (= 식재료 추가, 수정 시 사용하는 edittext 초기화)
    override fun onCancelButtonClicked() {
        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
        App.prefs.FoodCount = "1"
    }

    // 뒤로 가기를 누르면 메인화면으로 이동_ssh
    override fun onBackPressed() {
        // 메인 액티비티 하나만 실행하고 나머지 액티비티는 다 지움_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}