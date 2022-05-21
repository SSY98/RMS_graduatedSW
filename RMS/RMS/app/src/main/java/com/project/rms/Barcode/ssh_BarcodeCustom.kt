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
import com.project.rms.Recipe.ssy_Recipe_Litem
import com.theartofdev.edmodo.cropper.CropImageView
import org.json.JSONObject
import java.io.File
import java.net.URL

class ssh_BarcodeCustom : AppCompatActivity(), ssh_BarcodeDialogInterface, ssh_ReceiptDialogInterface {
    private lateinit var capture: CaptureManager
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    lateinit var db3 : ssh_ReceiptDatabase // 영수증 db_ssh
    var productList = mutableListOf<ssh_ProductEntity>() // 식재료 목록_ssh
    var ReceiptList = mutableListOf<ssh_ReceiptEntity>() // 영수증 목록_ssh
    var ReceiptItem = arrayListOf<ssh_Receipt_item>() // 영수증 목록을 식재료 목록 db에 추가하기 위한 배열_ssh

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

    class rept_BarThread(var bar:String) : Thread(){
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

                //유통기한 함수 _ ssy
                fun getDate(Date : String) : String{
                    var customDate = false
                    val BanDate = arrayOf<String>("˚C", "˚", "→","-", "도씨","시간","도",":",",","없음") //금지어 추가
                    val cal = Calendar.getInstance()

                    for(i in BanDate.indices){ //금지어 있으면 커스텀 모드
                        if(Date.contains(BanDate[i])){
                            customDate = true
                        }
                    }
                    if (customDate==false){
                        if(Date.contains("년")){ //년이 들어가있으면
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.YEAR, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else if(Date.contains("월")){ //월이 들어가있으면
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.MONTH, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else{ //일이 들어가있으면
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.DATE, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                    }
                    else{ //customDate가 true이면
                        var now_date = "사용자 직접 입력"
                        return now_date
                    }
                }
                //유통기한 함수
                // 이름, 종류, 유통기한에 대한 정보를 SharedPreferences를 활용해 임시 저장_ssh
                App.prefs.FoodName = PRDLST_NM
                App.prefs.FoodCategory = PRDLST_DCNM
                App.prefs.FoodDate = getDate(POG_DAYCNT)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 영수증 사진 crop_ssh
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                // crop 된 이미지의 uri를 비트맵으로 변환_ssh
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)

                datapath = "$filesDir/tesseract/"
                checkFile(File(datapath + "tessdata/"))
                tess = TessBaseAPI()
                tess!!.init(datapath, "eng")
                tess!!.setVariable("VAR_CHAR_WHITELIST", "1234567890")
                tess!!.setImage(bitmap)
                val text = tess!!.utF8Text
                Log.d("살려주세요",text)
                //tess.recycle() //다쓰고 삭제

                //인식한 바코드 barcode_arr에 넣기
                val t_arr = text.split(" ","\n")
                var barcode_arr = mutableListOf<String>()
                for(i in 0 .. t_arr.size-1){
                    t_arr[i].replace("[^\\d]".toRegex(),"")
                    if(t_arr[i].length == 13){
                        barcode_arr.add(t_arr[i])
                    }
                }

                //for문으로 배열안 바코드 가져오기
                for(i in 0 .. barcode_arr.size-1) {
                    var rept_Thread = rept_BarThread(barcode_arr[i])
                    rept_Thread.start()
                    rept_Thread.join() // join()을 사용하면 해당 스레드가 종료되기를 기다렸다가 다음으로 넘어감
                    val receipt = ssh_ReceiptEntity(null,App.prefs.FoodName.toString(),  App.prefs.FoodCategory.toString(), App.prefs.FoodDate.toString(), "1")
                    insertReceipt(receipt)
                    ReceiptItem.add(ssh_Receipt_item(null, App.prefs.FoodName.toString(), App.prefs.FoodCategory.toString(), App.prefs.FoodDate.toString(), "1"))

                    App.prefs.FoodName = ""
                    App.prefs.FoodCategory = ""
                    App.prefs.FoodDate = ""
                }
                Log.d("바코드만 추출",barcode_arr.toString())

                ReceiptDialog() // 영수증 인식 팝업창 출력_ssh

                Toast.makeText(
                    this, "Cropping successful, Sample: " + result.sampleSize, Toast.LENGTH_LONG).show()
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
                                else if(real_result == "계란"){
                                    App.prefs.FoodCategory = "계란"
                                    cal.add(Calendar.DATE, 25).toString()
                                    App.prefs.FoodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                }
                                else if(real_result == "당근"){
                                    App.prefs.FoodCategory = "당근"
                                    cal.add(Calendar.DATE, 14).toString()
                                    App.prefs.FoodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                }
                                else if(real_result == "양파"){
                                    App.prefs.FoodCategory = "양파"
                                    cal.add(Calendar.DATE, 14).toString()
                                    App.prefs.FoodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                                }
                                else {
                                    App.prefs.FoodName = real_result
                                    App.prefs.FoodCategory = real_result
                                    App.prefs.FoodDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
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
            Log.d("ReceiptList","$ReceiptList")
        }
    }

    // 데이터베이스에 영수증 식재료를 삭제_ssh
    fun deleteReceipt(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db3.ReceiptDAO().deleteAll()
            }.await()
            ReceiptList = db3.ReceiptDAO().getAll()
            Log.d("ReceiptList","$ReceiptList")
        }
    }

    // 영수증 인식 시 식재료 추가에 대한 팝업창 출력_ssh
    fun ReceiptDialog(){
        App.prefs.Dbtn = true
        val ReceiptDialog = ssh_ReceiptDialog(this, this)
        ReceiptDialog.show()
    }

    // 영수증 인식 팝업창에서 확인 버튼 클릭 시 식재료 목록 데이터베이스에 영수증 인식 한 식재료를 추가_ssh
    override fun onReceiptAddButtonClicked() {
        // 영수증 db의 식재료를 식재료 db에 추가
        for(i in 0 .. ReceiptItem.size-1){
            val product = ssh_ProductEntity(null,ReceiptItem[i].itemname, ReceiptItem[i].itemcategory, ReceiptItem[i].itemdate, ReceiptItem[i].itemcount)
            insertProduct(product)
        }
        deleteReceipt() // 영수증 db 내용 모두 삭제
        ReceiptItem.clear() // ReceiptItem 배열의 내용 모두 삭제

        // 메인 액티비티 하나만 실행하고 나머지 액티비티는 다 지움_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    // 영수증 인식 팝업창에서 삭제 버튼 클릭 시 receiptitem 배열에서 해당 식재료 삭제
    override fun onReceiptDialogDeleteListener() {
        for(i in 0 .. ReceiptItem.size-1) {
            if (App.prefs.ReceiptName == ReceiptItem[i].itemname && App.prefs.ReceiptCategory == ReceiptItem[i].itemcategory &&
                    App.prefs.ReceiptDate == ReceiptItem[i].itemdate && App.prefs.ReceiptCount == ReceiptItem[i].itemcount
                ) {
                val index = i
                ReceiptItem.removeAt(index)
                break
            }
            else {
                App.prefs.ReceiptName = ""
                App.prefs.ReceiptCategory = ""
                App.prefs.ReceiptDate = ""
                App.prefs.ReceiptCount = ""
            }
        }
        Log.d("ReceiptItem","$ReceiptItem")
    }

    // 영수증 인식 팝업창에서 취소 버튼 클릭 시 식재료 목록 데이터베이스에 영수증 인식 한 식재료를 추가_ssh
    override fun onReceiptCancelButtonClicked() {
        deleteReceipt() // 영수증 db 내용 모두 삭제
        ReceiptItem.clear() // ReceiptItem 배열의 내용 모두 삭제
    }

    // 영수증 인식 팝업창에서 추가 버튼을 클릭 시 식재료 목록 데이터베이스에 영수증 인식 한 식재료를 추가되고 바코드 인식 화면을 띄움_ssh
    override fun onReceiptPlusButtonClicked() {
        // 영수증 db의 식재료를 식재료 db에 추가
        for(i in 0 .. ReceiptItem.size-1){
            val product = ssh_ProductEntity(null,ReceiptItem[i].itemname, ReceiptItem[i].itemcategory, ReceiptItem[i].itemdate, ReceiptItem[i].itemcount)
            insertProduct(product)
        }
        deleteReceipt() // 영수증 db 내용 모두 삭제
        ReceiptItem.clear() // ReceiptItem 배열의 내용 모두 삭제
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