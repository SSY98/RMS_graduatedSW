package com.project.rms

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import com.project.rms.Barcode.ssh_BarcodeCustom
import com.project.rms.Barcode.ssh_BarcodeDialog
import com.project.rms.Barcode.ssh_BarcodeDialogInterface
import com.project.rms.CountdownTimer.ssy_Countdowntimer
import com.project.rms.Foodlist.Database.ssh_OnProductDeleteListener
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.Foodlist.Database.ssh_ProductEntity
import com.project.rms.Foodlist.ItemTouchHelperCallback
import com.project.rms.Foodlist.LinearListViewAdapter
import com.project.rms.Foodlist.ssh_FoodListAdapter
import com.project.rms.Memo.*
import com.project.rms.Newfeed.ExampleAdapter
import com.project.rms.Newfeed.ysj_ExampleModel
import com.project.rms.Recipe.ssy_RecipeActivity
import com.project.rms.Weather.ssy_WHEATHER
import com.project.rms.Weather.ssy_WeatherInterface
import com.project.rms.Webview.ssy_Webview
import com.project.rms.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
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

class MainActivity : AppCompatActivity(), ssh_BarcodeDialogInterface, ssh_OnProductDeleteListener,TextToSpeech.OnInitListener,
    ssh_OnMemoUpdateListener, ssh_OnMemoDeleteListener, ssh_MemoUpdateDialogInterface {
    //메인액티비티 뷰바인딩
    private lateinit var binding: ActivityMainBinding
    private val lm = LinearLayoutManager(this)

    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    lateinit var db2 : ssh_MemoDatabase // 메모 db_ssh
    var productList = mutableListOf<ssh_ProductEntity>() // 식재료 목록_ssh
    var memoList =  mutableListOf<ssh_MemoEntity>() // 메모 목록_ssh

    //음성인식_ssy
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognitionListener: RecognitionListener
    private var tts: TextToSpeech? = null //tts

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
                binding.rvAutoScrollContent.openAutoScroll(speed = 20, reverse = false)
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
        var base_time = getBaseTime(timeH, timeM)
        // 현재 시각이 00시이고 45분 이하여서 baseTime이 2330이면 어제 정보 받아오기
        if ((timeH == "00" || timeH == "01" || timeH == "02")&& base_time == "2300") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
            base_time = "2300"
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
                    binding.rainper.setText("강수확률 : "+api_POP+"%")
                    binding.temperatures.setText(api_TMP+" °C")

                    if (getRainType(api_PTY) == "없음"){
                        binding.raintype.setText(" ")
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

        //------------------------------------음성인식-----------------------------------------------
        requestPermission()
        var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        setListener()
        //음성인식 무한반복 스레드
        tts = TextToSpeech(this, this) //tts
        if(App.prefs.Voiceoption == true) {
            GlobalScope.launch(Dispatchers.Main) {
                while (App.prefs.Voiceoption == true){ //옵션으로 온오프 할수있는 변수
                    while (App.prefs.Voicepause == true) { //소리사용하는 동안 온오프 할수있는 변수
                        App.prefs.Voicetime = 0
                        speechRecognizer =
                            SpeechRecognizer.createSpeechRecognizer(applicationContext)
                        speechRecognizer.setRecognitionListener(recognitionListener)
                        speechRecognizer.startListening(intent)
                        delay(7000)
                        speechRecognizer.destroy()
                        delay(App.prefs.Voicetime)
                    }
                    delay(7000)
                }
            }
        }
        //------------------------------------음성인식_끝---------------------------------------------

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh
        db2 = ssh_MemoDatabase.getInstance(this)!! // 메모 db_ssh

        val customdialogtest= findViewById<ImageButton>(R.id.setting)
        val StartRecognition = findViewById<ImageButton>(R.id.BarcodeImageRecognition) // 바코드 이미지 인식 버튼_ssh
        val recipeB= findViewById<ImageButton>(R.id.recipe) //ssy
        val AddMemo = findViewById<ImageButton>(R.id.add_memo) // 메모 추가 버튼_ssh

        getAllProduct() // 데이터베이스에 있는 식재료를 불러옴_ssh
        getAllMemo() // 데이터베이스에 있는 메모를 불러옴_ssh

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
        //타이머_ssy
        binding.timer.setOnClickListener{
            val intent = Intent(this, ssy_Countdowntimer::class.java)
            startActivity(intent)
        }

        // 설정칸으로 dialog(POPUP 창) 테스트함_테스트용도
        customdialogtest.setOnClickListener{
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        //유튜브_ssy
        binding.youtubeB.setOnClickListener{
            val intent = Intent(this, ssy_Webview::class.java)
            App.prefs.WebSite = "https://www.youtube.com"
            startActivity(intent)
        }
        //웹서핑_ssy
        binding.websurfB.setOnClickListener{
            val intent = Intent(this, ssy_Webview::class.java)
            App.prefs.WebSite = "https://www.google.co.kr"
            startActivity(intent)
        }
        // 메모 추가 버튼 클릭 시 메모 추가_ssh
        AddMemo.setOnClickListener{
            val memo = ssh_MemoEntity(null, edittext_memo.text.toString())
            edittext_memo.setText("")
            insertMemo(memo)
            Log.d("memo", "hi3")
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

                //유통기한 함수 _ ssy
                fun getDate(Date : String) : String{
                    var customDate = false
                    val BanDate = arrayOf<String>("˚C", "˚", "→","-", "도씨","시간","도",":",",","없음","수출") //금지어 추가
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
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else if(Date.contains("월")){ //월이 들어가있으면
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.MONTH, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                        else{ //일이 들어가있으면
                            val number = Date.replace("[^\\d]".toRegex(), "")
                            cal.add(Calendar.DATE, number.toInt()).toString()
                            var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            return now_date
                        }
                    }
                    else{ //customDate가 true이면
                        var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                        return now_date
                    }
                }
                //유통기한 함수

                // 이름, 종류, 유통기한에 대한 정보를 SharedPreferences를 활용해 임시 저장_ssh
                App.prefs.FoodName = PRDLST_NM
                App.prefs.FoodCategory = PRDLST_DCNM
                App.prefs.FoodDate = getDate(POG_DAYCNT)

                val DateSplit = getDate(POG_DAYCNT).split("-")
                App.prefs.FoodYear = DateSplit[0]
                App.prefs.FoodMonth = DateSplit[1]
                App.prefs.FoodDay = DateSplit[2]

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
                App.prefs.Dbtn = true
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
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
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
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
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
        App.prefs.FoodYear = ""
        App.prefs.FoodMonth = ""
        App.prefs.FoodDay = ""
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

    //------------------------------------음성인식----------------------------------------------------
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private fun setListener() {
        recognitionListener = object: RecognitionListener {
            //음소거를 위한 오디오 매니저
            //주의: 혹시 avd에서 음성인식이 안될경우, (1) avd설정->마이크 설정 확인 (2) 어플 마이크 허용 여부 확인 (3) 구글 마이크로 한번 실험해보기
            override fun onReadyForSpeech(params: Bundle?) {
               volume_min()
            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {
                var message: String

                when (error) {
                    SpeechRecognizer.ERROR_AUDIO ->
                        message = "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT ->
                        message = "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        message = "퍼미션 없음"
                    SpeechRecognizer.ERROR_NETWORK ->
                        message = "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        message = "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        message = "찾을 수 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        message = "RECOGNIZER가 바쁨"
                    SpeechRecognizer.ERROR_SERVER ->
                        message = "서버가 이상함"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        message = "말하는 시간초과"
                    else ->
                        message = "알 수 없는 오류"
                }
                //Toast.makeText(applicationContext, "에러 발생 $message", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                var matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>
                val voice = matches[matches.size-1]
                Log.d("결과", matches[matches.size-1])
                if (voice.contains(App.prefs.Voicename)) {
                    App.prefs.Voiceanswer = true // 쉐어드프리퍼런스 사용하면 쉐어드로 옮기기
                    volume_max()
                    tts!!.speak("네, 부르셨어요.", TextToSpeech.QUEUE_FLUSH, null,"")
                    Log.d("결과", "네, 부르셨어요.")
                }
                else if(App.prefs.Voiceanswer == true){
                    val number = voice.replace("[^0-9]".toRegex(), "")
                    //음성인식으로 타이머실행
                    if(voice.contains("타이머") and voice.contains("실행")){
                        Log.d("결과", "타이머를 실행시킬게요.")
                        volume_max()
                        tts!!.speak("타이머를 실행시킬게요.", TextToSpeech.QUEUE_FLUSH, null,"")
                        if (number != ""){
                            if (voice.contains("분")and voice.contains("초")){
                                val intent2 = Intent(applicationContext, ssy_Countdowntimer::class.java)
                                val min = number.substring(0, number.length-2)
                                val sec = number.substring(number.length-2, number.length)
                                App.prefs.TimerSecond = min.toInt() * 60 + sec.toInt()
                                App.prefs.Voicereq = true
                                startActivity(intent2)
                            }
                            else if(voice.contains("분")){
                                val intent2 = Intent(applicationContext, ssy_Countdowntimer::class.java)
                                App.prefs.TimerSecond = number.toInt() * 60
                                App.prefs.Voicereq = true
                                startActivity(intent2)
                            }
                            else{
                                val intent2 = Intent(applicationContext, ssy_Countdowntimer::class.java)
                                App.prefs.TimerSecond = number.toInt()
                                App.prefs.Voicereq = true
                                startActivity(intent2)
                            }
                        }
                        else{
                            val intent2 = Intent(applicationContext, ssy_Countdowntimer::class.java)
                            startActivity(intent2)
                        }
                        App.prefs.Voiceanswer = false
                    }
                    //음성인식으로 레시피 실행
                    else if(voice.contains("레시피") and voice.contains("실행")){
                        Log.d("결과", "레시피를 실행시킬게요.")
                        volume_max()
                        tts!!.speak("레시피를 실행시킬게요.", TextToSpeech.QUEUE_FLUSH, null,"")
                        val intent2 = Intent(applicationContext, ssy_RecipeActivity::class.java)
                        startActivity(intent2)
                        App.prefs.Voiceanswer = false
                    }
                    //음성인식으로 유튜브실행
                    else if(voice.contains("유튜브") and voice.contains("실행")) {
                        Log.d("결과", "유튜브를 실행시킬게요.")
                        volume_max()
                        tts!!.speak("유튜브를 실행시킬게요.", TextToSpeech.QUEUE_FLUSH, null, "")
                        App.prefs.Voiceanswer = false
                        App.prefs.Voicereq = true
                        App.prefs.WebSite = "https://www.youtube.com"
                        val intent = Intent(applicationContext, ssy_Webview::class.java)
                        startActivity(intent)
                    }
                    //음성인식으로 구글실행
                    else if((voice.contains("구글")or voice.contains("웹")) and (voice.contains("실행")or voice.contains("검색"))) {
                        Log.d("결과", "구글을 실행시킬게요.")
                        volume_max()
                        tts!!.speak("구글을 실행시킬게요.", TextToSpeech.QUEUE_FLUSH, null, "")
                        App.prefs.Voiceanswer = false
                        App.prefs.Voicereq = true
                        App.prefs.WebSite = "https://www.google.co.kr"
                        val intent = Intent(applicationContext, ssy_Webview::class.java)
                        startActivity(intent)
                    }
                    //음성인식으로 식재료 추가
                    else if(voice.contains("등록")) {
                        var input = voice.split(" ")
                        var name = input[0]
                        var count = "1"
                        var count_array = arrayOf("한",'두',"세","네","다섯","여섯","일곱","여덟","아홉","열")
                        if(voice.replace("[^0-9]".toRegex(), "")==""){
                            for(i in 0 .. count_array.size-1){
                                if(voice.contains(count_array[i].toString())){
                                    count = (i+1).toString()
                                }
                            }
                        }
                        else{
                            count = voice.replace("[^0-9]".toRegex(), "")
                        }
                        //tts
                        volume_max()
                        App.prefs.Voicetime = 1000
                        tts!!.speak(name+count+" 개 "+"등록하겠습니다.", TextToSpeech.QUEUE_FLUSH, null, "")
                        App.prefs.Voicereq = true
                        //식재료 등록
                        App.prefs.Dbtn = false
                        App.prefs.FoodName = name
                        val cal = Calendar.getInstance()
                        var now_date = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                        /* 시연끝나고 원래대로 바꾸기
                        App.prefs.FoodCategory = name
                        App.prefs.FoodDate = now_date
                        App.prefs.FoodCount = count
                        */
                        if (App.prefs.FoodName == "사과") {
                            App.prefs.FoodCategory = "과일"
                            App.prefs.FoodCount = count
                            cal.add(Calendar.DATE, 21).toString()
                            App.prefs.FoodDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            val DateSplit = App.prefs.FoodDate.toString().split("-")
                            App.prefs.FoodYear = DateSplit[0]
                            App.prefs.FoodMonth = DateSplit[1]
                            App.prefs.FoodDay = DateSplit[2]
                        }
                        else if(App.prefs.FoodName == "계란"){
                            App.prefs.FoodCategory = "계란"
                            App.prefs.FoodCount = count
                            cal.add(Calendar.DATE, 25).toString()
                            App.prefs.FoodDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            val DateSplit = App.prefs.FoodDate.toString().split("-")
                            App.prefs.FoodYear = DateSplit[0]
                            App.prefs.FoodMonth = DateSplit[1]
                            App.prefs.FoodDay = DateSplit[2]
                        }
                        else if(App.prefs.FoodName == "당근"){
                            App.prefs.FoodCategory = "채소"
                            App.prefs.FoodCount = count
                            cal.add(Calendar.DATE, 14).toString()
                            App.prefs.FoodDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            val DateSplit = App.prefs.FoodDate.toString().split("-")
                            App.prefs.FoodYear = DateSplit[0]
                            App.prefs.FoodMonth = DateSplit[1]
                            App.prefs.FoodDay = DateSplit[2]
                        }
                        else if(App.prefs.FoodName == "양파"){
                            App.prefs.FoodCategory = "채소"
                            App.prefs.FoodCount = count
                            cal.add(Calendar.DATE, 14).toString()
                            App.prefs.FoodDate = SimpleDateFormat("yyyy-M-d", Locale.getDefault()).format(cal.time)
                            val DateSplit = App.prefs.FoodDate.toString().split("-")
                            App.prefs.FoodYear = DateSplit[0]
                            App.prefs.FoodMonth = DateSplit[1]
                            App.prefs.FoodDay = DateSplit[2]
                        }
                        else {
                            App.prefs.FoodCategory = name
                            App.prefs.FoodDate = now_date
                            App.prefs.FoodCount = count
                        }
                        dialog()

                    }
                    //음성인식 명령 예외처리
                    else{
                        volume_max()
                        tts!!.speak("죄송해요. 잘모르겠어요.", TextToSpeech.QUEUE_FLUSH, null,"")
                        App.prefs.Voiceanswer = true
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }

        }
    }
    fun volume_min(){
        val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val muteValue = AudioManager.ADJUST_MUTE
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
    }
    fun volume_max(){
        App.prefs.Voicetime = 700
        val audioManager: AudioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val muteValue = AudioManager.ADJUST_UNMUTE
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, muteValue, 0)
    }
    //음성인식 무한반복

    //tts
    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.KOREA)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
    public override fun onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    //------------------------------------음성인식_끝------------------------------------------------- 
    // ------------------------------------메모장_ssh-------------------------------------------------
    // 데이터베이스에 있는 메모 추가
    fun insertMemo(memo : ssh_MemoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db2.memoDAO().insert(memo)
                memoList.add(memo)
                Log.d("memo", "hi1")
            }.await()
            getAllMemo()
        }
    }

    // 데이터베이스에 있는 메모를 불러옴
    fun getAllMemo(){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                memoList = db2.memoDAO().getAll()
                Log.d("memo","$memoList")
            }.await()
            CoroutineScope(Dispatchers.Main).launch {
                setMemoRecyclerView(memoList)
            }
        }
    }

    // 데이터베이스에 있는 메모를 수정
    fun updateMemo(memo : ssh_MemoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db2.memoDAO().update(memo)
            }.await()
            getAllMemo()
        }
    }

    // 데이터베이스에 있는 메모를 삭제
    fun deleteMemo(memo: ssh_MemoEntity){
        CoroutineScope(Dispatchers.IO).launch {
            async{
                db2.memoDAO().delete(memo)
            }.await()
            getAllMemo()
        }
    }

    // recyclerview로 데이터베이스에 있는 메모 출력_ssh
    fun setMemoRecyclerView(memoList : MutableList<ssh_MemoEntity>) {
        val recyclerView = findViewById<RecyclerView>(R.id.Memo_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ssh_MemoAdapter(memoList, this, this)//수정
        recyclerView.adapter = adapter//++
    }

    // 스와이프 모션을 사용하면 데이터베이스 내의 메모가 삭제됨
    override fun onMemoDeleteListener(memo: ssh_MemoEntity) {
        deleteMemo(memo)
    }

    // 메모 수정 화면 dialog를 출력
    override fun onMemoUpdateListener(memo: ssh_MemoEntity) {
        val MemoDialog = ssh_MemoUpdateDialog(this,this)
        MemoDialog.show()
    }
    
    // 메모 팝업창에서 수정 버튼 클릭
    override fun onMemoUpdateButtonClicked() {
        // 메모 내용 수정 사항을 각각의 변수에 저장
        val currentMemoID = App.prefs.MemoID?.toLong()
        var currentMemo = App.prefs.MemoContents.toString()

        // 변수에 저장된 수정 사항을 데이터베이스에 적용
        val memo = ssh_MemoEntity(currentMemoID, currentMemo)
        updateMemo(memo)

        // SharedPreference 변수에 저장된 메모 내용 초기화 (= 메모 수정 시 사용하는 edittext 초기화)
        App.prefs.MemoContents = ""
    }

    // 메모 팝업창에서 취소 버튼 클릭
    override fun onMemoCancelButtonClicked() {
        App.prefs.MemoContents = ""
    }
    // ------------------------------------메모장_ssh-------------------------------------------------
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