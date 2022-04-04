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
import com.project.rms.Barcode.ssh_BarcodeDialog
import com.project.rms.Barcode.ssh_BarcodeDialogInterface
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Recipe.ssy_RecipeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity(), ssh_BarcodeDialogInterface {
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var productList = listOf<ssh_ProductEntity>() // 식재료 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        val customdialogtest= findViewById<ImageButton>(R.id.setting)
        val StartRecognition = findViewById<Button>(R.id.BarcodeImageRecognition) // 바코드 이미지 인식 버튼_ssh
        val recipeB= findViewById<Button>(R.id.recipe) //ssy
        val image_recognition= findViewById<Button>(R.id.image_t)//ysj

        getAllProduct() // 데이터베이스에 있는 식재료를 불러옴_ssh


        //리스트 뷰
        /*val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
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
        })*/

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

    // 바코드 api_ssy,ssh
    inner class BarcodeThread(var bar:String) : Thread(){
        override fun run() {
            var Api_key = "1937954c9b7840bbbf76"
            var site =
                "https://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/C005/json/1/2/BAR_CD="+bar
            var url = URL(site)
            var conn = url.openConnection()
            var input = conn.getInputStream()
            var isr = InputStreamReader(input)
            var br = BufferedReader(isr)

            var str: String? = null
            var buf = StringBuffer()

            do {
                str = br.readLine()

                if (str != null) {
                    buf.append(str)
                }
            } while (str != null)

            var root = JSONObject(buf.toString()) //받아온 내용 객체로 가져오기
            var C005 = root.getJSONObject("C005") // 내용에서 C005객체 가져오기
            var total_count: String = C005.getString("total_count") //검색결과 갯수 가져오기

            //바코드번호로 정상적이게 검색이 되었으면 파싱시작
            if(total_count=="1"){
                var row = C005.getJSONArray("row") //row라는 배열 가져오기
                var obj2 = row.getJSONObject(0)
                var result = C005.getJSONObject("RESULT")
                var code: String = result.getString("CODE") //결과코드 가져오기

                var PRDLST_NM: String = obj2.getString("PRDLST_NM")
                var POG_DAYCNT: String = obj2.getString("POG_DAYCNT")
                var BAR_CD: String = obj2.getString("BAR_CD")
                var PRDLST_DCNM: String = obj2.getString("PRDLST_DCNM")

                // 이름, 종류, 유통기한에 대한 정보를 SharedPreferences를 활용해 임시 저장_ssh
                App.prefs.FoodName = PRDLST_NM
                App.prefs.FoodCategory = PRDLST_DCNM
                App.prefs.FoodDate = POG_DAYCNT

                // 바코드 인식한 상품 로그 출력
                Log.d("바코드_번호:","${BAR_CD}")
                Log.d("바코드_제품이름:","${PRDLST_NM}")
                Log.d("바코드_제품종류:","${PRDLST_DCNM}")
                Log.d("바코드_유통기한:","${POG_DAYCNT}")
            }
            // 바코드번호로 검색이 되지않았으면 실패메시지 발생
            else{
                Log.d("바코드_상태:","바코드를 다시입력해주세요")
            }
        }
    }

    // 바코드 스캔 결과 값을 받아 처리_ssh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                var B_API_thread = BarcodeThread(result.contents.toString())
                B_API_thread.start()
                B_API_thread.join() // join()을 사용하면 해당 스레드가 종료되기를 기다렸다가 다음으로 넘어감
                dialog()
            } else {
                Toast.makeText(this, "취소", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // 바코드 스캔 시 식재료 추가에 대한 팝업창 출력_ssh
    fun dialog(){
        val BarcodeDialog = ssh_BarcodeDialog(this,this)
        BarcodeDialog.show()
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
    fun setRecyclerView(productList : List<ssh_ProductEntity>){
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LinearListViewAdapter(productList)//수정
        recyclerView.layoutManager = LinearLayoutManager(this)//++
        val callback = ItemTouchHelperCallback(adapter,this)//++
        val touchHelper = ItemTouchHelper(callback)//++
        touchHelper.attachToRecyclerView(recyclerView)//++
        recyclerView.adapter = adapter//++
        //밑++
        adapter.startDrag(object : LinearListViewAdapter.OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper.startDrag(viewHolder)
            }
        })
    }

    // 바코드 인식 후 팝업창에서 추가 버튼을 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가된다._ssh
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
    }

    // 바코드 인식 팝업창에서 취소 버튼을 누르면 시행되는 작업
    override fun onCancelButtonClicked() {

    }

    override fun onPlusButtonClicked() {
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

        var integrator = IntentIntegrator(this)
        integrator.setPrompt("바코드를 스캔하세요")
        integrator.setCameraId(0) // 0이면 후면 카메라, 1이면 전면 카메라
        integrator.captureActivity = ssh_BarcodeCustom::class.java // 커스텀한 바코드 화면
        integrator.initiateScan() // initiateScan()을 통해 Zxing 라이브러리 바코드 스캐너가 보여짐
    }
}