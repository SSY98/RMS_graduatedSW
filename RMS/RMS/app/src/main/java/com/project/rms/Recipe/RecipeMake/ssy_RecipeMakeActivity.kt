package com.project.rms.Recipe.RecipeMake

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.project.rms.App
import com.project.rms.R
import com.project.rms.databinding.SsyActivityRecipemakeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.DecimalFormat

class ssy_RecipeMakeActivity : AppCompatActivity() {
    private lateinit var binding:SsyActivityRecipemakeBinding
    var itemList = arrayListOf<ssy_RecipeMake_Litem>()
    var listAdapter = ssy_RecipeMake_Ladapter(itemList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SsyActivityRecipemakeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infoList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.infoList.adapter = listAdapter

        //GlobalScope시작
        GlobalScope.launch(Dispatchers.IO) {
            var t_site = StringBuilder()
            var Api_key = "1937954c9b7840bbbf76"
            var site =
                "http://openapi.foodsafetykorea.go.kr/api/" + Api_key + "/COOKRCP01/json/1/1000/RCP_NM="
            t_site.append(site)
            t_site.append(App.prefs.Recipe_Name) //쉐어드프리퍼런스

            Log.d("site:", "${t_site}")
            var url = URL(t_site.toString())
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
            var COOKRCP01 = root.getJSONObject("COOKRCP01") // 내용에서 C005객체 가져오기
            var total_count: String = COOKRCP01.getString("total_count") //검색결과 갯수 가져오기

            var result = COOKRCP01.getJSONObject("RESULT")
            var code: String = result.getString("CODE") //결과코드 가져오기
            Log.d("레시피_결과:", "${code}")

            var row = COOKRCP01.getJSONArray("row") //row라는 배열 가져오기
            for(i in 0 until total_count.toInt()) {
                var obj2 = row.getJSONObject(i)
                var RCP_NM: String = obj2.getString("RCP_NM")
                var ATT_FILE_NO_MAIN: String = obj2.getString("ATT_FILE_NO_MAIN")
                var RCP_PARTS_DTLS: String = obj2.getString("RCP_PARTS_DTLS")
                var RCP_SEQ: String = obj2.getString("RCP_SEQ")
                Log.d("레시피_메뉴이름:","${RCP_NM}")
                Log.d("레시피_사진:","${ATT_FILE_NO_MAIN}")
                Log.d("레시피_필요재료:","${RCP_PARTS_DTLS}")
                if(RCP_SEQ==App.prefs.Recipe_Seq){ //쉐어드프리퍼런스로 seq와 비교
                    GlobalScope.launch(Dispatchers.Main){
                        val name= findViewById<TextView>(R.id.food_name) //ssy
                        val matrial= findViewById<TextView>(R.id.food_material) //ssy
                        val foodimg= findViewById<ImageView>(R.id.food_img) //ssy
                        name.setText(RCP_NM)
                        matrial.setText(RCP_PARTS_DTLS)
                        Glide.with(applicationContext)
                            .load(ATT_FILE_NO_MAIN)
                            .into(foodimg)
                    }
                    for(j in 1 until 21){
                        val numform = DecimalFormat("00")
                        var MANUAL: String = obj2.getString("MANUAL"+numform.format(j))
                        var MANUAL_IMG: String = obj2.getString("MANUAL_IMG"+numform.format(j))
                        if(MANUAL==""){
                            Log.d("레시피_만드는법 "+numform.format(j)+":","")
                        }
                        else{
                            if(MANUAL_IMG==""){ //사진없을때 디폴트값 이미지 호스팅
                                MANUAL_IMG="https://i.imgur.com/NuFJnf2.png#.Yjs_XV41ER8.link"
                            }
                            GlobalScope.launch(Dispatchers.Main){
                                itemList.add(ssy_RecipeMake_Litem(MANUAL_IMG,MANUAL))
                                listAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
        //GlobalScope끝
        listAdapter.notifyDataSetChanged()
    }
}