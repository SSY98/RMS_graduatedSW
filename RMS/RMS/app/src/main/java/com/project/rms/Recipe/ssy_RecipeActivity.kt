package com.project.rms.Recipe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
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
    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var materials = mutableListOf<String>()
    var check_materials = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SsyActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvList.adapter = listAdapter

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh

        //터치하면 그에 관한 정보 출력
        listAdapter.setItemClickListener(object: ssy_Recipe_Ladapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                //띄어쓰기 인식안돼서 앞글자만 따옴
                var input = itemList[position].name
                println(input)
                var token = input.split(' ')
                App.prefs.Recipe_Name = token[0]
                //앞글자 같은것중에 레시피번호 비교
                App.prefs.Recipe_Seq = itemList[position].seq

                Toast.makeText(applicationContext,
                    "${itemList[position].name}\n${itemList[position].material}\n"+token[0],
                    Toast.LENGTH_SHORT).show()

                val intent = Intent(applicationContext, ssy_RecipeMakeActivity::class.java)
                startActivity(intent)
            }
        })
        //globalscope 시작
        GlobalScope.launch(Dispatchers.IO) {
            check_materials = db.productDAO().date() //db의 값 가져오기
            //검색이 되는 재료들만 선정해서 재료 리스트에 넣음
            for(ck in 0 until check_materials.size){
                if(checkparts(check_materials[ck])!=0){
                    materials.add(check_materials[ck])
                }
            }
            //재료리스트가 비어있으면 랜덤한 200개만 출력
            if(materials.isEmpty()){
                empty_ref()
            }
            //재료리스트가 비어있지않으면 그에맞는 레시피 출력
            else{
                nonempty_ref()
            }
        }
        //globalscope끝
    }
    //냉장고가 비어있을때
    fun empty_ref(){
        //랜덤 출력
        val end = (200..1358).random()
        val start = end-200
        var t_site = StringBuilder()
        var Api_key = "1937954c9b7840bbbf76"
        var site = "http://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/COOKRCP01/json/"+start+"/"+end+"/"
        t_site.append(site)

        var url = URL(t_site.toString())
        var conn = url.openConnection()
        var input = conn.getInputStream()
        var isr = InputStreamReader(input)
        var br = BufferedReader(isr)

        Log.d("시발",t_site.toString())
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

        var row = COOKRCP01.getJSONArray("row") //row라는 배열 가져오기
        for(i in 0 until 200) {
            var obj2 = row.getJSONObject(i)
            var RCP_NM: String = obj2.getString("RCP_NM")
            var ATT_FILE_NO_MAIN: String = obj2.getString("ATT_FILE_NO_MAIN")
            var RCP_PARTS_DTLS: String = obj2.getString("RCP_PARTS_DTLS")
            var RCP_SEQ: String = obj2.getString("RCP_SEQ")
            GlobalScope.launch(Dispatchers.Main){
                    itemList.add(ssy_Recipe_Litem(RCP_SEQ, RCP_NM, RCP_PARTS_DTLS,ATT_FILE_NO_MAIN))
                    listAdapter.notifyDataSetChanged()
            }

        }
    }
    //냉장고가 안비어 있을때
    fun nonempty_ref(){
        var t_site = StringBuilder()
        var Api_key = "1937954c9b7840bbbf76"
        var site = "http://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/COOKRCP01/json/1/1000/"
        t_site.append(site)
        t_site.append("RCP_PARTS_DTLS="+materials[0])
        for(k in 1 until materials.size){
            t_site.append("&RCP_PARTS_DTLS="+materials[k])
        }

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

        //예외처리
        if(materials.size==1){
            materials.add(" ")
            materials.add(" ")
        }
        else if(materials.size==2){
            materials.add(" ")
        }

        var row = COOKRCP01.getJSONArray("row") //row라는 배열 가져오기
        for(i in 0 until total_count.toInt()) {
            var obj2 = row.getJSONObject(i)
            var RCP_NM: String = obj2.getString("RCP_NM")
            var ATT_FILE_NO_MAIN: String = obj2.getString("ATT_FILE_NO_MAIN")
            var RCP_PARTS_DTLS: String = obj2.getString("RCP_PARTS_DTLS")
            var RCP_SEQ: String = obj2.getString("RCP_SEQ")
            if(RCP_PARTS_DTLS.contains(materials[0]) or RCP_PARTS_DTLS.contains(materials[1]) or RCP_PARTS_DTLS.contains(materials[2])) {
                GlobalScope.launch(Dispatchers.Main){
                    itemList.add(ssy_Recipe_Litem(RCP_SEQ, RCP_NM, RCP_PARTS_DTLS,ATT_FILE_NO_MAIN))
                    listAdapter.notifyDataSetChanged()
                }
            }
        }
    }
    //받은 재료가 검색되나 안되나 확인
    fun checkparts(prats:String): Int {
        var t_site = StringBuilder()
        var Api_key = "1937954c9b7840bbbf76"
        var site = "http://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/COOKRCP01/json/1/1000/"
        t_site.append(site)

        t_site.append("RCP_PARTS_DTLS="+prats)

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

        return total_count.toInt()
    }
}
