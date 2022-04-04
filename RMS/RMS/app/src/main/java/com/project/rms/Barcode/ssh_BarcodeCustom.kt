package com.project.rms.Barcode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.CaptureManager
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Image_recognition.ResponseData
import com.project.rms.Image_recognition.retrofit
import com.project.rms.Image_recognition.retrofit_interface
import com.project.rms.MainActivity
import com.project.rms.R
import com.project.rms.SwipeActivity
import com.project.rms.databinding.ActivitySshBarcodeCustomBinding
import com.project.rms.databinding.YsjImageRecognitionBinding
import kotlinx.android.synthetic.main.activity_ssh_barcode_custom.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat

class ssh_BarcodeCustom : AppCompatActivity(), ssh_BarcodeDialogInterface {
    private lateinit var capture: CaptureManager
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var productList = listOf<ssh_ProductEntity>() // 식재료 목록_ssh

    //ysj 추가
    // ViewBinding
    lateinit var binding : ActivitySshBarcodeCustomBinding

    val server = retrofit.create(retrofit_interface::class.java) //api

    // Permisisons
    val PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val PERMISSIONS_REQUEST = 100

    // Request Code
    private val BUTTON4 = 400

    private var photoUri: Uri? = null
    //private lateinit var img : ImageView //
    //ysj 추가 끝

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySshBarcodeCustomBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        checkPermissions(PERMISSIONS, PERMISSIONS_REQUEST)

        //이미지 버튼 누르면 카메라 실행 ysj
        binding.camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = File(
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image").apply {
                    if (!this.exists()) {
                        this.mkdirs()
                    }
                },
                newJpgFileName()//이미지 파일 저장
            )
            photoUri = FileProvider.getUriForFile(
                this,
                "com.blacklog.takepicture.fileprovider",
                photoFile
            )
            takePictureIntent.resolveActivity(packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, BUTTON4)
            }
        }


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //RESULT_OK 사진 촬영을 했을 때 ysj
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {

                BUTTON4 -> {
                    val file = File("/storage/emulated/0/Android/data/com.project.rms/files/Pictures/image/test.jpg")
                    val requestFile = RequestBody.create(MediaType.parse("application/pdf"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    Log.d("결과", "5")
                    server.sendFile(body).enqueue(object : Callback<ResponseData> {
                        override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                            if(response.isSuccessful){
                                // 정상적으로 통신이 성고된 경우
                                var result: ResponseData? = response.body()
                                var str_data = result?.toString()
                                var str_arr = str_data?.split("=",")")
                                // ex) real_result -> 사과
                                var real_result = str_arr?.get(1)

                                Log.d("YMC", "onResponse 성공: " + result?.toString());
                                binding.result.setText(real_result.toString())
                            }else{
                                // 통신이 실패한 경우(응답코드 3xx, 4xx 등)
                                Log.d("YMC", "onResponse 실패")
                            }
                        }

                        override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                            // 통신 실패 (인터넷 끊킴, 예외 발생 등 시스템적인 이유)
                            Log.d("YMC", "onFailure 에러: " + t.message.toString());
                        }
                    })
                    Log.d("결과", "6")
                }

            }
        }

    }

    //이미지 파일 저장 ysj
    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = "test"
        //val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    //권한체크 ysj
    private fun checkPermissions(permissions: Array<String>, permissionsRequest: Int): Boolean {
        val permissionList : MutableList<String> = mutableListOf()
        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission)
            }
        }
        if(permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), PERMISSIONS_REQUEST)
            return false
        }
        return true
    }


}