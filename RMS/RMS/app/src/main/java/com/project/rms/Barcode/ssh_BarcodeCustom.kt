package com.project.rms.Barcode

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.CaptureManager
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.MainActivity
import com.project.rms.R
import com.project.rms.SwipeActivity
import kotlinx.android.synthetic.main.activity_ssh_barcode_custom.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ssh_BarcodeCustom : AppCompatActivity(), ssh_BarcodeDialogInterface {
    private lateinit var capture: CaptureManager
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var productList = listOf<ssh_ProductEntity>() // 식재료 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ssh_barcode_custom)

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        val direct_add= findViewById<Button>(R.id.direct_add) // 직접 입력 버튼

        // 직접 입력 버튼 누르면 팝업창 출력
        direct_add.setOnClickListener{
            val BarcodeDialog = ssh_BarcodeDialog(this,this)
            BarcodeDialog.show()
        }

        // CaptureManager에 DecorateBarcodeView를 연결시켜준뒤 decode함
        capture = CaptureManager(this, scanner_custom)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // 데이터베이스에 식재료를 추가_ssh
    fun insertProduct(product : ssh_ProductEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.productDAO().insert(product)
            }.await()
            getAllProduct()
        }
    }

    // 데이터베이스에 있는 식재료를 불러옴_ssh
    fun getAllProduct(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                productList = db.productDAO().getAll()
                Log.d("product","$productList")
            }.await()
        }
    }

    override fun onAddButtonClicked() {
        var productname = App.prefs.FoodName.toString()
        var productcatergory = App.prefs.FoodCategory.toString()
        var productdate = App.prefs.FoodDate.toString()
        var productcount = App.prefs.FoodCount.toString()

        val product = ssh_ProductEntity(null, productname, productcatergory, productdate, productcount)
        insertProduct(product)

        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodCount = "1"

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCancelButtonClicked() {
        TODO("Not yet implemented")
    }
}