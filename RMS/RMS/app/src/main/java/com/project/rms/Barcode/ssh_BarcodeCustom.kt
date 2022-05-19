package com.project.rms.Barcode

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.journeyapps.barcodescanner.CaptureManager
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Image_recognition.ResponseData
import com.project.rms.Image_recognition.retrofit
import com.project.rms.Image_recognition.retrofit_interface
import com.project.rms.MainActivity
import com.project.rms.databinding.ActivitySshBarcodeCustomBinding
import com.theartofdev.edmodo.cropper.CropImage
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
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*

import com.googlecode.tesseract.android.TessBaseAPI
import com.project.rms.Memo.ssh_MemoEntity
import com.project.rms.R
import com.project.rms.Receipt.*
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File

class ssh_BarcodeCustom : AppCompatActivity(), ssh_BarcodeDialogInterface, ssh_ReceiptDialogInterface {
    private lateinit var capture: CaptureManager
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    lateinit var db3 : ssh_ReceiptDatabase // 영수증 db_ssh
    var productList = mutableListOf<ssh_ProductEntity>() // 식재료 목록_ssh
    var ReceiptList = mutableListOf<ssh_ReceiptEntity>() // 영수증 목록_ssh

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
    private val BUTTON = 100

    private var photoUri: Uri? = null
    //private lateinit var img : ImageView //
    //ysj 추가 끝

    //영수증인식_ssy
    private var tess //Tess API reference
            : TessBaseAPI? = null
    var datapath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db3 = ssh_ReceiptDatabase.getInstance(this)!! // 영수증 db_ssh

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

        //영수증 버튼 누르면 카메라 실행_ssh
        binding.receipt.setOnClickListener {
            // 이미지 크롭을 위한 가이드를 열어주어 크롭할 이미지를 받아올 수 있게한다.
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
            /*val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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
            // 이미지의 uri를 받아서 해당하는 이미지를 크롭한다.
            CropImage.activity(photoUri)
                .start(this);

            takePictureIntent.resolveActivity(packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, BUTTON)
            }*/
        }


        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        val direct_add= findViewById<ImageButton>(R.id.direct_add) // 직접 입력 버튼

        // 직접 입력 버튼 누르면 팝업창 출력_ssh
        direct_add.setOnClickListener{
            App.prefs.Dbtn = true
            val BarcodeDialog = ssh_BarcodeDialog(this,this)
            BarcodeDialog.show()
        }

        // CaptureManager에 DecorateBarcodeView를 연결시켜준뒤 decode함_ssh
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

    // 직접 입력 팝업창에서 확인 버튼 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가된다._ssh
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

        // 메인 액티비티 하나만 실행하고 나머지 액티비티는 다 지움_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    // 직접 입력 팝업창에서 취소 버튼을 누르면 SharedPreference 변수에 저장된 식재료 이름, 종류, 유통기한, 갯수 초기화 (= 식재료 추가, 수정 시 사용하는 edittext 초기화)_ssh
    override fun onCancelButtonClicked() {
        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodCount = "1"
    }

    // 직접 입력 팝업창에서 추가 버튼을 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가되고 바코드 인식 화면을 띄움_ssh
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 영수증 사진 crop_ssh
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                // crop 된 이미지의 uri를 비트맵으로 변환_ssh
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)
                //(findViewById<View>(R.id.quick_start_cropped_image) as ImageView).setImageBitmap(bitmap)

                // 비트맵 이미지를 jpeg 형식의 파일로 변환_ssh
                val FilePath = "/storage/emulated/0/Android/data/com.project.rms/files/Pictures/image/receipt.jpeg"

                // 파일 객체를 생성한 후 파일 객체가 가르키는 위치에 디렉토리가 없으면 디렉토리를 생성_ssh
                val file = File(FilePath)
                if (!file.exists()) file.mkdirs()

                // 디렉토리 안에 새로운 파일을 만든 후 그 파일에 bitmap을 outputStream으로 작성_ssh
                val fileCacheItem = File(FilePath)
                var out: OutputStream? = null

                try {
                    fileCacheItem.createNewFile()
                    out = FileOutputStream(fileCacheItem)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        out?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                datapath = "$filesDir/tesseract/"
                checkFile(File(datapath + "tessdata/"))
                tess = TessBaseAPI()
                tess!!.init(datapath, "eng")
                tess!!.setVariable("VAR_CHAR_WHITELIST", "1234567890")
                tess!!.setImage(bitmap)
                val text = tess!!.utF8Text
                Log.d("살려주세요",text)
                //tess.recycle() //다쓰고 삭제
                val t_arr = text.split(" ","\n")
                var barcode_arr = mutableListOf<String>()
                for(i in 0 .. t_arr.size-1){
                    t_arr[i].replace("[^\\d]".toRegex(),"")
                    if(t_arr[i].length == 13){
                        barcode_arr.add(t_arr[i])
                    }
                }
                Log.d("바코드만 추출",barcode_arr.toString())


                Log.d("file","${fileCacheItem.name}")
                Log.d("file","${fileCacheItem.absolutePath}")
                Log.d("file","${file.isDirectory}")
                Log.d("file","${fileCacheItem.isFile}")

                Toast.makeText(
                    this, "Cropping successful, Sample: " + result.sampleSize, Toast.LENGTH_LONG).show()
                val receipt = ssh_ReceiptEntity(null,"1", "1", "2022-05-30", "1")
                insertReceipt(receipt)

                ReceiptDialog() // 영수증 인식 팝업창 출력_ssh
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.error, Toast.LENGTH_LONG).show()
            }
        }

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
                                // ex) real_result -> 사과 (이미지 인식 결과)
                                var real_result = str_arr?.get(1)

                                Log.d("YMC", "onResponse 성공: " + result?.toString());

                                // 이름, 종류, 유통기한에 대한 정보를 SharedPreferences를 활용해 임시 저장_ssh
                                val cal = Calendar.getInstance()
                                App.prefs.FoodName = real_result

                                if (real_result == "사과") {
                                    App.prefs.FoodCategory = "과일"
                                    cal.add(Calendar.DATE, 21).toString()
                                    App.prefs.FoodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                }
                                else {
                                    App.prefs.FoodName = ""
                                    App.prefs.FoodCategory = ""
                                    App.prefs.FoodDate = ""
                                }

                                dialog() // 팝업창 실행
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
    //tess사용하기위한 함수
    private val langFileName = "eng.traineddata"
    private fun copyFiles() {
        try {
            val filepath = datapath + "tessdata/" + langFileName
            val assetManager = assets
            val instream: InputStream = assetManager.open("tessdata/"+langFileName)
            val outstream: OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var read: Int
            while (instream.read(buffer).also { read = it } != -1) {
                outstream.write(buffer, 0, read)
            }
            outstream.flush()
            outstream.close()
            instream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun checkFile(dir: File) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles()
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if (dir.exists()) {
            val datafilepath = datapath + "tessdata/" + langFileName
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles()
            }
        }
    }
    //tess사용하기위한 함수

    // 데이터베이스에 영수증 식재료를 추가_ssh
    fun insertReceipt(receipt : ssh_ReceiptEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db3.ReceiptDAO().insert(receipt)
            }.await()
            ReceiptList = db3.ReceiptDAO().getAll()
        }
    }



    // 영수증 인식 시 식재료 추가에 대한 팝업창 출력_ssh
    fun ReceiptDialog(){
        App.prefs.Dbtn = true
        val ReceiptDialog = ssh_ReceiptDialog(this, this)
        ReceiptDialog.show()
    }



    override fun onReceiptAddButtonClicked() {
        TODO("Not yet implemented")
    }

    override fun onReceiptCancelButtonClicked() {
        TODO("Not yet implemented")
    }

    override fun onReceiptPlusButtonClicked() {
    }

    // 이미지 인식 시 식재료 추가에 대한 팝업창 출력_ssh
    fun dialog(){
        App.prefs.Dbtn = true
        val ImageDialog = ssh_BarcodeDialog(this,this)
        ImageDialog.show()
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

    // 뒤로 가기를 누르면 메인화면으로 이동_ssh
    override fun onBackPressed() {
        // 메인 액티비티 하나만 실행하고 나머지 액티비티는 다 지움_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}