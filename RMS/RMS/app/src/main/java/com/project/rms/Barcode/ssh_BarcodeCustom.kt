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
import com.google.zxing.integration.android.IntentIntegrator
import java.io.*

import com.googlecode.tesseract.android.TessBaseAPI
import com.project.rms.Image_recognition.*
import com.project.rms.Memo.ssh_MemoEntity
import com.project.rms.R
import com.project.rms.Receipt.*
import com.project.rms.Recipe.ssy_Recipe_Litem
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageView
import org.json.JSONObject
import java.io.File
import java.net.URL

class ssh_BarcodeCustom : AppCompatActivity(), ssh_BarcodeDialogInterface, ssh_ReceiptDialogInterface {
    private lateinit var capture: CaptureManager
    lateinit var db : ssh_ProductDatabase // ????????? db_ssh
    lateinit var db3 : ssh_ReceiptDatabase // ????????? db_ssh
    lateinit var db_Img : ssh_ImageDatabase // ??????????????? db
    var productList = mutableListOf<ssh_ProductEntity>() // ????????? ??????_ssh
    var ReceiptList = mutableListOf<ssh_ReceiptEntity>() // ????????? ??????_ssh
    var ImageList = mutableListOf<ssh_ImageEntity>() // ????????? ??????_ssh
    var ReceiptItem = arrayListOf<ssh_Receipt_item>() // ????????? ????????? ????????? ?????? db??? ???????????? ?????? ??????_ssh

    //ysj ??????
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
    //ysj ?????? ???

    //???????????????_ssy
    private var tess //Tess API reference
            : TessBaseAPI? = null
    var datapath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db3 = ssh_ReceiptDatabase.getInstance(this)!! // ????????? db_ssh

        binding = ActivitySshBarcodeCustomBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        checkPermissions(PERMISSIONS, PERMISSIONS_REQUEST)

        //????????? ?????? ????????? ????????? ?????? ysj
        binding.camera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = File(
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image").apply {
                    if (!this.exists()) {
                        this.mkdirs()
                    }
                },
                newJpgFileName()//????????? ?????? ??????
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

        //????????? ?????? ????????? ????????? ??????_ssh
        binding.receipt.setOnClickListener {
            deleteReceipt()
            // ????????? ????????? ?????? ???????????? ???????????? ????????? ???????????? ????????? ??? ????????????.
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
            Toast.makeText(applicationContext, "????????? ????????? ??????????????? ???????????? ????????? ????????? ???????????? ????????? ??? ????????? ????????? ??? ???????????? ????????? ???????????????.", Toast.LENGTH_LONG).show()
            /*val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = File(
                File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "image").apply {
                    if (!this.exists()) {
                        this.mkdirs()
                    }
                },
                newJpgFileName()//????????? ?????? ??????
            )
            photoUri = FileProvider.getUriForFile(
                this,
                "com.blacklog.takepicture.fileprovider",
                photoFile
            )
            // ???????????? uri??? ????????? ???????????? ???????????? ????????????.
            CropImage.activity(photoUri)
                .start(this);

            takePictureIntent.resolveActivity(packageManager)?.also {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, BUTTON)
            }*/
        }


        db = ssh_ProductDatabase.getInstance(this)!! // ????????? db_ssh
        db_Img = ssh_ImageDatabase.getInstance(this)!! // ????????? ?????? db

        val direct_add= findViewById<ImageButton>(R.id.direct_add) // ?????? ?????? ??????

        // ?????? ?????? ?????? ????????? ????????? ??????_ssh
        direct_add.setOnClickListener{
            App.prefs.Dbtn = true
            App.prefs.FoodName = ""
            App.prefs.FoodCategory = ""
            App.prefs.FoodDate = ""
            App.prefs.FoodYear = ""
            App.prefs.FoodMonth = ""
            App.prefs.FoodDay = ""
            App.prefs.FoodCount = "1"
            val BarcodeDialog = ssh_BarcodeDialog(this,this)
            BarcodeDialog.show()
        }

        // CaptureManager??? DecorateBarcodeView??? ?????????????????? decode???_ssh
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

    // ????????????????????? ???????????? ??????_ssh
    fun insertProduct(product : ssh_ProductEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db.productDAO().insert(product)
            }.await()
            getAllProduct()
        }
    }

    // ????????????????????? ?????? ???????????? ?????????_ssh
    fun getAllProduct(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                productList = db.productDAO().getAll()
                Log.d("product","$productList")
            }.await()
        }
    }

    // ?????? ?????? ??????????????? ?????? ?????? ????????? ??????????????? ????????? ????????? ????????????????????? ????????????._ssh
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
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
        App.prefs.FoodCount = "1"

        // ?????? ???????????? ????????? ???????????? ????????? ??????????????? ??? ??????_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    // ?????? ?????? ??????????????? ?????? ????????? ????????? SharedPreference ????????? ????????? ????????? ??????, ??????, ????????????, ?????? ????????? (= ????????? ??????, ?????? ??? ???????????? edittext ?????????)_ssh
    override fun onCancelButtonClicked() {
        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
        App.prefs.FoodCount = "1"
    }

    // ?????? ?????? ??????????????? ?????? ????????? ????????? ??????????????? ????????? ????????? ????????????????????? ???????????? ????????? ?????? ????????? ??????_ssh
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
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
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

            var root = JSONObject(buf.toString()) //????????? ?????? ????????? ????????????
            var C005 = root.getJSONObject("C005") // ???????????? C005?????? ????????????
            var total_count: String = C005.getString("total_count") //???????????? ?????? ????????????

            //?????????????????? ??????????????? ????????? ???????????? ????????????
            if(total_count=="1"){
                var row = C005.getJSONArray("row") //row?????? ?????? ????????????
                var obj2 = row.getJSONObject(0)
                var result = C005.getJSONObject("RESULT")
                var code: String = result.getString("CODE") //???????????? ????????????

                var PRDLST_NM: String = obj2.getString("PRDLST_NM")
                var POG_DAYCNT: String = obj2.getString("POG_DAYCNT")
                var BAR_CD: String = obj2.getString("BAR_CD")
                var PRDLST_DCNM: String = obj2.getString("PRDLST_DCNM")

                //???????????? ?????? _ ssy
                fun getDate(Date : String) : String{
                    var customDate = false
                    val BanDate = arrayOf<String>("??C", "??", "???","-", "??????","??????","???",":",",","??????","??????") //????????? ??????
                    val cal = Calendar.getInstance()

                    for(i in BanDate.indices){ //????????? ????????? ????????? ??????
                        if(Date.contains(BanDate[i])){
                            customDate = true
                        }
                    }
                    if (customDate==false){
                        if(Date.contains("???")){ //?????? ??????????????????
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.YEAR, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else if(Date.contains("???")){ //?????? ??????????????????
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.MONTH, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else{ //?????? ??????????????????
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.DATE, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                    }
                    else{ //customDate??? true??????
                        var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                        return now_date
                    }
                }
                //???????????? ??????
                // ??????, ??????, ??????????????? ?????? ????????? SharedPreferences??? ????????? ?????? ??????_ssh
                App.prefs.FoodName = PRDLST_NM
                App.prefs.FoodCategory = PRDLST_DCNM
                App.prefs.FoodDate = getDate(POG_DAYCNT)

                // ????????? ????????? ?????? ?????? ??????
                Log.d("?????????_??????:","${BAR_CD}")
                Log.d("?????????_????????????:","${PRDLST_NM}")
                Log.d("?????????_????????????:","${PRDLST_DCNM}")
                Log.d("?????????_????????????:","${POG_DAYCNT}")
            }
            // ?????????????????? ????????? ?????????????????? ??????????????? ??????
            else{
                Log.d("?????????_??????:","???????????? ????????????????????????")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // ????????? ?????? crop_ssh
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                // crop ??? ???????????? uri??? ??????????????? ??????_ssh
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)

                datapath = "$filesDir/tesseract/"
                checkFile(File(datapath + "tessdata/"))
                tess = TessBaseAPI()
                tess!!.init(datapath, "eng")
                tess!!.setVariable("VAR_CHAR_WHITELIST", "1234567890")
                tess!!.setImage(bitmap)
                val text = tess!!.utF8Text
                Log.d("???????????????",text)
                //tess.recycle() //????????? ??????

                //????????? ????????? barcode_arr??? ??????
                val t_arr = text.split(" ","\n")
                var barcode_arr = mutableListOf<String>()
                for(i in 0 .. t_arr.size-1){
                    t_arr[i].replace("[^\\d]".toRegex(),"")
                    if(t_arr[i].length == 13){
                        barcode_arr.add(t_arr[i])
                    }
                }

                //for????????? ????????? ????????? ????????????
                for(i in 0 .. barcode_arr.size-1) {
                    var rept_Thread = rept_BarThread(barcode_arr[i])
                    rept_Thread.start()
                    rept_Thread.join() // join()??? ???????????? ?????? ???????????? ??????????????? ??????????????? ???????????? ?????????
                    val receipt = ssh_ReceiptEntity(null,App.prefs.FoodName.toString(),  App.prefs.FoodCategory.toString(), App.prefs.FoodDate.toString(), "1")
                    insertReceipt(receipt)
                    ReceiptItem.add(ssh_Receipt_item(null, App.prefs.FoodName.toString(), App.prefs.FoodCategory.toString(), App.prefs.FoodDate.toString(), "1"))

                    App.prefs.FoodName = ""
                    App.prefs.FoodCategory = ""
                    App.prefs.FoodDate = ""
                }
                Log.d("???????????? ??????",barcode_arr.toString())

                ReceiptDialog() // ????????? ?????? ????????? ??????_ssh
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "????????? ?????? ??????." + result.error, Toast.LENGTH_LONG).show()
            }
        }

        //RESULT_OK ?????? ????????? ?????? ??? ysj
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                BUTTON4 -> {
                    val file = File("/storage/emulated/0/Android/data/com.project.rms/files/Pictures/image/test.jpg")
                    val requestFile = RequestBody.create(MediaType.parse("application/pdf"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    Log.d("??????", "5")
                    server.sendFile(body).enqueue(object : Callback<ResponseData> {
                        override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                            if(response.isSuccessful){
                                // ??????????????? ????????? ????????? ??????
                                var result: ResponseData? = response.body()
                                var str_data = result?.toString()
                                var str_arr = str_data?.split("=",")")
                                // ex) real_result -> ?????? (????????? ?????? ??????)
                                var real_result = str_arr?.get(1)

                                Log.d("YMC", "onResponse ??????: " + result?.toString());

                                // ??????, ??????, ??????????????? ?????? ????????? SharedPreferences??? ????????? ?????? ??????_ssh
                                val cal = Calendar.getInstance()
                                App.prefs.FoodName = real_result
                                CoroutineScope(Dispatchers.IO).launch {
                                    ImageInfoAdd()
                                    ImageList = db_Img.ImageDAO().getAll()
                                    Log.d("image_haha","$ImageList")
                                    for(i in 0 until ImageList.size){
                                        if(ImageList[i].name == real_result){
                                            App.prefs.FoodCategory = ImageList[i].category
                                            cal.add(Calendar.DATE, ImageList[i].date).toString()
                                            App.prefs.FoodDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                                            val DateSplit = App.prefs.FoodDate.toString().split("-")
                                            App.prefs.FoodYear = DateSplit[0]
                                            App.prefs.FoodMonth = DateSplit[1]
                                            App.prefs.FoodDay = DateSplit[2]
                                        }
                                    }
                                    CoroutineScope(Dispatchers.Main).launch {
                                        dialog() // ????????? ??????
                                    }
                                } //db???????????????
                            }else{
                                // ????????? ????????? ??????(???????????? 3xx, 4xx ???)
                                Log.d("YMC", "onResponse ??????")
                                Toast.makeText(applicationContext, "????????? ??????????????????.\n????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                            // ?????? ?????? (????????? ??????, ?????? ?????? ??? ??????????????? ??????)
                            Log.d("YMC", "onFailure ??????: " + t.message.toString());
                        }
                    })
                    Log.d("??????", "6")
                }

            }
        }
    }
    //??????????????? ????????? ????????????
    fun ImageInfoAdd(){
        class SetItem(val name:String, val category: String, val date: Int)
        var SetList = mutableListOf<SetItem>()
        SetList.add(SetItem("??????","??????",14))
        SetList.add(SetItem("?????????","??????",10))
        SetList.add(SetItem("??????","??????",7))
        SetList.add(SetItem("??????","??????",14))
        SetList.add(SetItem("??????","??????",7))
        SetList.add(SetItem("??????","??????",5))
        var count = 0
        CoroutineScope(Dispatchers.IO).launch {
            count = db_Img.ImageDAO().getCount()
            if(count==SetList.size){}
            else{
                db_Img.ImageDAO().deleteAll()
                for(i in 0 until SetList.size){
                    val image = ssh_ImageEntity(null, SetList[i].name, SetList[i].category, SetList[i].date)
                    CoroutineScope(Dispatchers.IO).launch {
                        async{
                            db_Img.ImageDAO().insert(image)
                        }.await()}
                }
            }
        }
    }
    //tess?????????????????? ??????
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
        //??????????????? ????????? ??????????????? ????????? ????????? ????????? ??????
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles()
        }
        //??????????????? ????????? ????????? ????????? ???????????? ??????
        if (dir.exists()) {
            val datafilepath = datapath + "tessdata/" + langFileName
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles()
            }
        }
    }
    //tess?????????????????? ??????

    // ????????????????????? ????????? ???????????? ??????_ssh
    fun insertReceipt(receipt : ssh_ReceiptEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db3.ReceiptDAO().insert(receipt)
            }.await()
            ReceiptList = db3.ReceiptDAO().getAll()
            Log.d("ReceiptList","$ReceiptList")
        }
    }

    // ????????????????????? ????????? ???????????? ??????_ssh
    fun deleteReceipt(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db3.ReceiptDAO().deleteAll()
            }.await()
            ReceiptList = db3.ReceiptDAO().getAll()
            Log.d("ReceiptList","$ReceiptList")
        }
    }

    // ????????? ?????? ??? ????????? ????????? ?????? ????????? ??????_ssh
    fun ReceiptDialog(){
        App.prefs.Dbtn = true
        val ReceiptDialog = ssh_ReceiptDialog(this, this)
        ReceiptDialog.show()
    }

    // ????????? ?????? ??????????????? ?????? ?????? ?????? ??? receiptitem ???????????? ?????? ????????? ??????
    override fun onReceiptDialogDeleteListener() {
        for(i in 0 .. ReceiptItem.size-1) {
            if (App.prefs.ReceiptName == ReceiptItem[i].itemname && App.prefs.ReceiptCategory == ReceiptItem[i].itemcategory &&
                App.prefs.ReceiptDate == ReceiptItem[i].itemdate && App.prefs.ReceiptCount == ReceiptItem[i].itemcount
            ) {
                val index = i
                ReceiptItem.removeAt(index)
                App.prefs.ReceiptName = ""
                App.prefs.ReceiptCategory = ""
                App.prefs.ReceiptDate = ""
                App.prefs.ReceiptCount = ""
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

    // ????????? ?????? ??????????????? ?????? ?????? ?????? ??? ????????? ?????? ????????????????????? ????????? ?????? ??? ???????????? ??????_ssh
    override fun onReceiptAddButtonClicked() {
        // ????????? db??? ???????????? ????????? db??? ??????
        for(i in 0 .. ReceiptItem.size-1){
            val product = ssh_ProductEntity(null,ReceiptItem[i].itemname, ReceiptItem[i].itemcategory, ReceiptItem[i].itemdate, ReceiptItem[i].itemcount)
            insertProduct(product)
        }
        deleteReceipt() // ????????? db ?????? ?????? ??????
        ReceiptItem.clear() // ReceiptItem ????????? ?????? ?????? ??????

        // ?????? ???????????? ????????? ???????????? ????????? ??????????????? ??? ??????_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    // ????????? ?????? ??????????????? ?????? ?????? ?????? ??? ????????? ?????? ????????????????????? ????????? ?????? ??? ???????????? ??????_ssh
    override fun onReceiptCancelButtonClicked() {
        deleteReceipt() // ????????? db ?????? ?????? ??????
        ReceiptItem.clear() // ReceiptItem ????????? ?????? ?????? ??????
    }

    // ????????? ?????? ??????????????? ?????? ????????? ?????? ??? ????????? ?????? ????????????????????? ????????? ?????? ??? ???????????? ???????????? ????????? ?????? ????????? ??????_ssh
    override fun onReceiptPlusButtonClicked() {
        // ????????? db??? ???????????? ????????? db??? ??????
        for(i in 0 .. ReceiptItem.size-1){
            val product = ssh_ProductEntity(null,ReceiptItem[i].itemname, ReceiptItem[i].itemcategory, ReceiptItem[i].itemdate, ReceiptItem[i].itemcount)
            insertProduct(product)
        }
        deleteReceipt() // ????????? db ?????? ?????? ??????
        ReceiptItem.clear() // ReceiptItem ????????? ?????? ?????? ??????

        var integrator = IntentIntegrator(this)
        integrator.setPrompt("???????????? ???????????????")
        integrator.setCameraId(0) // 0?????? ?????? ?????????, 1?????? ?????? ?????????
        integrator.captureActivity = ssh_BarcodeCustom::class.java // ???????????? ????????? ??????
        integrator.initiateScan() // initiateScan()??? ?????? Zxing ??????????????? ????????? ???????????? ?????????
    }

    // ????????? ?????? ??? ????????? ????????? ?????? ????????? ??????_ssh
    fun dialog(){
        App.prefs.Dbtn = true
        val ImageDialog = ssh_BarcodeDialog(this,this)
        ImageDialog.show()
    }
    //????????? ?????? ?????? ysj
    private fun newJpgFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = "test"
        //val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    //???????????? ysj
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

    // ?????? ????????? ????????? ?????????????????? ??????_ssh
    override fun onBackPressed() {
        // ?????? ???????????? ????????? ???????????? ????????? ??????????????? ??? ??????_ssh
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}