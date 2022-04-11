package com.project.rms

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import com.project.rms.Barcode.ssh_BarcodeCustom
import com.project.rms.Barcode.ssh_BarcodeDialog
import com.project.rms.Barcode.ssh_BarcodeDialogInterface
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Newfeed.ExampleAdapter
import com.project.rms.Newfeed.ysj_ExampleModel
import com.project.rms.Recipe.ssy_RecipeActivity
import com.project.rms.Weather.ssy_WHEATHER
import com.project.rms.Weather.ssy_WeatherInterface
import com.project.rms.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import org.naver.Naver
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ssh_BarcodeDialogInterface, ssh_OnProductDeleteListener {
    //메인액티비티 뷰바인딩
    private lateinit var binding: ActivityMainBinding
    private val lm = LinearLayoutManager(this)

    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var productList = mutableListOf<ssh_ProductEntity>() // 식재료 목록_ssh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //------------------------------------뉴스_시작-----------------------------------------------
        val naver = Naver(clientId = "ESLGojTe7I4Uriq_7nfX", clientSecret = "sm7PyE1Nmq")
        GlobalScope.launch(Dispatchers.IO) {
            //출처: 네이버뉴스 sdk - https://github.com/kimsuelim/naver-sdk-kotlin
            val search_results = naver.search().news(query = "속보")
            val ntarray = Array<String>(5,{""})
            val nlarray = Array<String>(5,{""})
            search_results.items.forEach { news -> news.title }
            for (i in 0 until 5) {
                var newstitle = search_results.items[i].title.toString()
                newstitle = newstitle.replace("&quot;", "")
                newstitle = newstitle.replace("<b>", "")
                newstitle = newstitle.replace("</b>", "")
                ntarray[i]=newstitle
                nlarray[i]=search_results.items[i].link
            }
            GlobalScope.launch(Dispatchers.Main){
                var a1 = arrayOf(ntarray[0],nlarray[0])
                var a2 = arrayOf(ntarray[1],nlarray[1])
                var a3 = arrayOf(ntarray[2],nlarray[2])
                var a4 = arrayOf(ntarray[3],nlarray[3])
                var a5 = arrayOf(ntarray[4],nlarray[4])

                binding.rvAutoScrollContent.setLoopEnabled(true)
                binding.rvAutoScrollContent.openAutoScroll(speed = 12, reverse = false)
                binding.rvAutoScrollContent.setCanTouch(true)
                lm.orientation = LinearLayoutManager.VERTICAL
                binding.rvAutoScrollContent.layoutManager = lm

                fun setUpAutoScrollContent(messagesList: List<ysj_ExampleModel>, onItemClicked: (String) -> Unit) {

                    val adapter = ExampleAdapter().apply {
                        submitList(messagesList)
                    }
                    binding.rvAutoScrollContent.adapter = adapter
                    binding.rvAutoScrollContent.setItemClickListener { viewHolder, position ->
                        viewHolder?.let {
                            adapter.onLinkItem(viewHolder, position, onItemClicked)
                            if(position == 0){
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(a1[1]))
                                startActivity(intent)
                            }else if(position == 1){
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(a2[1]))
                                startActivity(intent)
                            }else if(position == 2){
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(a3[1]))
                                startActivity(intent)
                            } else if(position == 3){
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(a4[1]))
                                startActivity(intent)
                            } else if(position == 4){
                                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(a5[1]))
                                startActivity(intent)
                            }
                        }
                    }
                }
                setUpAutoScrollContent(
                    listOf(
                        ysj_ExampleModel(a1[0]), ysj_ExampleModel(a2[0]),ysj_ExampleModel(a3[0]),ysj_ExampleModel(a4[0]),ysj_ExampleModel(a5[0])
                    )
                ) {
                    //뉴스피드 예외처리 구간
                }
            }
        }
        //------------------------------------뉴스_끝------------------------------------------------

        //------------------------------------날씨_시작-----------------------------------------------
        val cal = Calendar.getInstance()
        var base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val timeH = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시각
        val timeM = SimpleDateFormat("mm", Locale.getDefault()).format(cal.time) // 현재 분
        // API 가져오기 적당하게 변환
        Log.d("api_times", timeH+timeM)
        val base_time = getBaseTime(timeH, timeM)
        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if (timeH == "00" && base_time == "2300") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }
        val call = ApiObject.retrofitService.GetWeather("JSON", 10, 1, base_date, base_time, "59", "119")
        call.enqueue(object : retrofit2.Callback<ssy_WHEATHER>{
            override fun onResponse(call: Call<ssy_WHEATHER>, response: Response<ssy_WHEATHER>) {
                if (response.isSuccessful){
                    Log.d("api", response.body().toString())
                    Log.d("api_date", base_date)
                    Log.d("api_time", base_time)
                    Log.d("api", response.body()!!.response.body.items.item.toString())
                    val api_TMP = response.body()!!.response.body.items.item[0].fcstValue
                    val api_SKY = response.body()!!.response.body.items.item[5].fcstValue
                    val api_PTY = response.body()!!.response.body.items.item[6].fcstValue
                    val api_POP = response.body()!!.response.body.items.item[7].fcstValue
                    binding.weathertext.setText(getSky(api_SKY))
                    binding.raintype.setText(getRainType(api_PTY))
                    binding.rainper.setText("강수확률은 "+api_POP+"% 입니다")
                    binding.temperatures.setText(api_TMP+" °C")

                    if (getRainType(api_PTY) == "없음"){
                        if (getSky(api_SKY) == "맑음"){
                            binding.weatherimg.setImageResource(R.drawable.sunny)
                        }
                        else if (getSky(api_SKY) == "구름 많음"){
                            binding.weatherimg.setImageResource(R.drawable.overcast)
                        }
                        else if (getSky(api_SKY) == "흐림"){
                            binding.weatherimg.setImageResource(R.drawable.sunny)
                        }
                    }else if (getRainType(api_PTY) == "비"){
                        binding.weatherimg.setImageResource(R.drawable.rain)
                    }
                    else if (getRainType(api_PTY) == "비/눈"){
                        binding.weatherimg.setImageResource(R.drawable.rainsnow)
                    }
                    else if (getRainType(api_PTY) == "눈"){
                        binding.weatherimg.setImageResource(R.drawable.snow)
                    }
                    else if (getRainType(api_PTY) == "소나기"){
                        binding.weatherimg.setImageResource(R.drawable.shower)
                    }

                    Log.d("api_TMP", "기온: "+api_TMP+"도씨")
                    Log.d("api_SKY", "하늘: "+getSky(api_SKY))
                    Log.d("api_PTY", "비타입: "+getRainType(api_PTY))
                    Log.d("api_POP", "강수확률: "+api_POP+"%")
                }
            }
            override fun onFailure(call: Call<ssy_WHEATHER>, t: Throwable) {
                Log.d("api fail : ", t.message.toString())
            }
        })
        //------------------------------------날씨_끝------------------------------------------------

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        val customdialogtest= findViewById<ImageButton>(R.id.setting)
        val StartRecognition = findViewById<Button>(R.id.BarcodeImageRecognition) // 바코드 이미지 인식 버튼_ssh
        val recipeB= findViewById<Button>(R.id.recipe) //ssy

        getAllProduct() // 데이터베이스에 있는 식재료를 불러옴_ssh

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

        // 설정칸으로 dialog(POPUP 창) 테스트함_테스트용도
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
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LinearListViewAdapter(productList, this)//수정
        val callback = ItemTouchHelperCallback(adapter,this)//++
        val touchHelper = ItemTouchHelper(callback)//++
        touchHelper.attachToRecyclerView(recyclerView)//++
        recyclerView.adapter = adapter
    }

    // 바코드 인식 후 팝업창에서 확인 버튼을 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가된다._ssh
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

    // 바코드 인식 팝업창에서 취소 버튼을 누르면 SharedPreference 변수에 저장된 식재료 이름, 종류, 유통기한, 갯수 초기화 (= 식재료 추가, 수정 시 사용하는 edittext 초기화)_ssh
    override fun onCancelButtonClicked() {
        App.prefs.FoodName = ""
        App.prefs.FoodCategory = ""
        App.prefs.FoodDate = ""
        App.prefs.FoodCount = "1"
    }

    // 바코드 인식 후 팝업창에서 추가 버튼을 누르면 팝업창에서 입력한 내용이 데이터베이스에 추가되고 바코드 인식 화면을 띄움_ssh
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

    // 스와이프 모션을 사용하면 데이터베이스 내의 식재료가 삭제됨
    override fun onProductDeleteListener(product: ssh_ProductEntity) {
        deleteProduct(product)
    }
}

//--------------------------------날씨_ssy-------------------------------------------------------
fun getRainType(rainType : String): String{
    return when(rainType) {
        "0" -> "없음"
        "1" -> "비"
        "2" -> "비/눈"
        "3" -> "눈"
        "4" -> "소나기"
        else -> "오류 rainType : " + rainType
        //없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
    }
}
fun getSky(sky : String) : String {
    return when(sky) {
        "1" -> "맑음"
        "3" -> "구름 많음"
        "4" -> "흐림"
        else -> "오류 rainType : " + sky
    }
}
fun getBaseTime(h: String, m : String) : String{
    val time = h+m
    return when(time.toInt()) {
        in 210 .. 510 -> "0200"
        in 510 .. 710 -> "0500"
        in 810 .. 1110 -> "0800"
        in 1110 .. 1410 -> "1100"
        in 1410 .. 1710 -> "1400"
        in 1710 .. 2010 -> "1700"
        in 2010 .. 2310 -> "2000"
        else -> "2300"
    }
}
//gson 객체 생성
var gson = GsonBuilder().setLenient().create()
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/") // 마지막 / 반드시 들어가야 함
    .addConverterFactory(GsonConverterFactory.create(gson)) // converter 지정
    .build() // retrofit 객체 생성
//오브젝트 생성
object ApiObject {
    val retrofitService: ssy_WeatherInterface by lazy {
        retrofit.create(ssy_WeatherInterface::class.java)
    }
}
//--------------------------------날씨_ssy-------------------------------------------------------