package com.project.rms.Recipe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.rms.Recipe.RecipeMake.ssy_RecipeMakeActivity
import com.project.rms.databinding.SsyActivityRecipeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class ssy_RecipeActivity : AppCompatActivity() {
    private lateinit var binding: SsyActivityRecipeBinding
    var itemList = arrayListOf<ssy_Recipe_Litem>()
    var listAdapter = ssy_Recipe_Ladapter(itemList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SsyActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvList.adapter = listAdapter

        //터치하면 그에 관한 정보 출력
        listAdapter.setItemClickListener(object: ssy_Recipe_Ladapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                var input = itemList[position].name
                println(input)
                var token = input.split(' ')
                Toast.makeText(applicationContext,
                    "${itemList[position].name}\n${itemList[position].material}\n"+token[0],
                    Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, ssy_RecipeMakeActivity::class.java)
                startActivity(intent)
                //쉐어드로 이름 앞에 값보내기 seq와 이름앞글자
            }
        })
        //globalscope 시작
        GlobalScope.launch(Dispatchers.IO) {
            var t_site = StringBuilder()
            var Api_key = "1937954c9b7840bbbf76"
            var site = "http://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/COOKRCP01/json/1/1000/RCP_PARTS_DTLS="
            t_site.append(site)

            //나중에 데이터베이스와 연관짓기
            var materials = mutableListOf<String>()
            materials.add("감자")
            materials.add("양파")
            materials.add("당근")
            t_site.append(materials[0])
            for(k in 1 until materials.size){
                t_site.append("&RCP_PARTS_DTLS="+materials[k])
            }

            Log.d("site:","${t_site}")
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
            Log.d("레시피_결과:","${code}")

            var row = COOKRCP01.getJSONArray("row") //row라는 배열 가져오기
            for(i in 0 until total_count.toInt()) {
                var obj2 = row.getJSONObject(i)
                var RCP_NM: String = obj2.getString("RCP_NM")
                var ATT_FILE_NO_MAIN: String = obj2.getString("ATT_FILE_NO_MAIN")
                var RCP_PARTS_DTLS: String = obj2.getString("RCP_PARTS_DTLS")
                var RCP_SEQ: String = obj2.getString("RCP_SEQ")
                if(RCP_PARTS_DTLS.contains(materials[0]) and RCP_PARTS_DTLS.contains(materials[1]) and RCP_PARTS_DTLS.contains(materials[2])) {
                    Log.d("레시피_메뉴이름:", "${RCP_NM}")
                    Log.d("레시피_사진:", "${ATT_FILE_NO_MAIN}")
                    Log.d("레시피_필요재료:", "${RCP_PARTS_DTLS}")
                    GlobalScope.launch(Dispatchers.Main){
                        itemList.add(ssy_Recipe_Litem(RCP_SEQ, RCP_NM, RCP_PARTS_DTLS,ATT_FILE_NO_MAIN))
                        listAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        //globalscope끝
    }
}