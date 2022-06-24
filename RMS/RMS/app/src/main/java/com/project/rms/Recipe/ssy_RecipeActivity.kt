package com.project.rms.Recipe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.rms.App
import com.project.rms.Foodlist.Database.ssh_ProductDatabase
import com.project.rms.R
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
    //결과
    var itemList = arrayListOf<ssy_Recipe_Litem>()
    var listAdapter = ssy_Recipe_Ladapter(itemList)
    //검색옵션
    var sel_itemList = arrayListOf<ssy_Parts_Litem>()
    var sel_listAdapter = ssy_Parts_Ladapter(sel_itemList)

    lateinit var db : ssh_ProductDatabase // 식재료 db_ssh
    var materials = mutableListOf<String>() //값이 정제된 리스트
    var check_materials = mutableListOf<String>() //맨처음 받는 리스트
    var choice_materials = mutableListOf<String>() //선택된 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SsyActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvList.adapter = listAdapter

        binding.selectList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.selectList.adapter = sel_listAdapter

        db = ssh_ProductDatabase.getInstance(this)!! // 식재료 db_ssh
        //재료가 선택되었을 때
        sel_listAdapter.setItemClickListener(object : ssy_Parts_Ladapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                if (choice_materials.size < 3) {
                    if (sel_itemList[position].parts in choice_materials) {
                        choice_materials.remove(sel_itemList[position].parts)
                    }
                    else {
                        choice_materials.add(sel_itemList[position].parts)
                    }
                    var selecttext = ""
                    for(i in 0..choice_materials.size-1){
                        selecttext = selecttext + choice_materials[i] + " "
                    }
                    binding.select.setText(selecttext)
                } else {
                    if (sel_itemList[position].parts in choice_materials) {
                        choice_materials.remove(sel_itemList[position].parts)
                    }
                    else{
                        Toast.makeText(
                            applicationContext,
                            "재료는 3개 까지만 선택가능합니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    var selecttext = ""
                    for(i in 0..choice_materials.size-1){
                        selecttext = selecttext + choice_materials[i] + " "
                    }
                    binding.select.setText(selecttext)
                }
            }
        })
        val dialog = ssy_LoadingDialog(ssy_RecipeActivity@this) //로딩 다이알로그
        //선택가능한 재료만 재료리스트에 넣기
        GlobalScope.launch(Dispatchers.IO) {
            check_materials = db.productDAO().date() //db의 값 가져오기
            //검색이 되는 재료들만 선정해서 재료 리스트에 넣음
            GlobalScope.launch(Dispatchers.Main){dialog.show()} //로딩 다이알로그 시작
            for(ck in 0 until check_materials.size){
                if(checkparts(check_materials[ck])!=0){
                    if(materials.contains(check_materials[ck])){

                    }
                    else{
                        materials.add(check_materials[ck])
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main){dialog.dismiss()} //로딩 다이알로그 해제
            for(i in 0 until materials.size){
                sel_itemList.add(ssy_Parts_Litem(materials[i]))
            }
            GlobalScope.launch(Dispatchers.Main){
                sel_listAdapter.notifyDataSetChanged()
                if (materials.size == 0) {
                    binding.receiptintro.setText("식재료를 추가해주세요")
                }
            }
        }
        //검색 버튼이 눌렸을때
        binding.search.setOnClickListener{
            GlobalScope.launch(Dispatchers.IO) {
                itemList.clear()
                if(choice_materials.isEmpty()){
                    empty_ref()
                }
                //재료리스트가 비어있지않으면 그에맞는 레시피 출력
                else{
                    nonempty_ref()
                }
            }
        }
        //터치하면 그에 관한 정보 출력
        listAdapter.setItemClickListener(object: ssy_Recipe_Ladapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                // 클릭 시 이벤트 작성
                //띄어쓰기 인식안돼서 앞글자만 따옴
                var input = itemList[position].name
                println(input)
                var token = input.split(' ','&')
                App.prefs.Recipe_Name = token[0]
                //앞글자 같은것중에 레시피번호 비교
                App.prefs.Recipe_Seq = itemList[position].seq
                val intent = Intent(applicationContext, ssy_RecipeMakeActivity::class.java)
                startActivity(intent)
            }
        })
    }
    //냉장고가 비어있을때
    fun empty_ref(){
        //랜덤 출력
        GlobalScope.launch(Dispatchers.Main){
            itemList.clear()
            listAdapter.notifyDataSetChanged()
        }
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
            //레시피 정렬, 보유재료 표시
            var ready = ""
            var ready_parts = 0
            for(i in 0 until materials.size){
                if(RCP_PARTS_DTLS.contains(materials[i])){
                    ready = ready + " " + materials[i]
                    ready_parts += 1
                }
            }
            if(ready == ""){
                ready = " 없음"
            }
            RCP_PARTS_DTLS = "[보유 식재료 :"+ ready + "]" +"\n\n"+RCP_PARTS_DTLS
            var RCP_SEQ: String = obj2.getString("RCP_SEQ")
            GlobalScope.launch(Dispatchers.Main){
                itemList.add(ssy_Recipe_Litem(RCP_SEQ, RCP_NM, RCP_PARTS_DTLS,ATT_FILE_NO_MAIN,ready_parts))
                itemList.sortBy { it.readyparts }
                itemList.reverse()
                listAdapter.notifyDataSetChanged()
            }

        }
    }
    //냉장고가 안비어 있을때
    @SuppressLint("ResourceAsColor")
    fun nonempty_ref(){
        GlobalScope.launch(Dispatchers.Main){
            itemList.clear()
            listAdapter.notifyDataSetChanged()
        }
        var t_site = StringBuilder()
        var Api_key = "1937954c9b7840bbbf76"
        var site = "http://openapi.foodsafetykorea.go.kr/api/"+Api_key+"/COOKRCP01/json/1/1000/"
        t_site.append(site)
        t_site.append("RCP_PARTS_DTLS="+choice_materials[0])
        for(k in 1 until choice_materials.size){
            t_site.append("&RCP_PARTS_DTLS="+choice_materials[k])
        }

        var count = 0
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
        if(choice_materials.size==1){
            choice_materials.add(" ")
            choice_materials.add(" ")
        }
        else if(choice_materials.size==2){
            choice_materials.add(" ")
        }

        var row = COOKRCP01.getJSONArray("row") //row라는 배열 가져오기
        for(i in 0 until total_count.toInt()) {
            var obj2 = row.getJSONObject(i)
            var RCP_NM: String = obj2.getString("RCP_NM")
            var ATT_FILE_NO_MAIN: String = obj2.getString("ATT_FILE_NO_MAIN")
            var RCP_PARTS_DTLS: String = obj2.getString("RCP_PARTS_DTLS")
            var RCP_SEQ: String = obj2.getString("RCP_SEQ")
            if(RCP_PARTS_DTLS.contains(choice_materials[0]) and RCP_PARTS_DTLS.contains(choice_materials[1]) and RCP_PARTS_DTLS.contains(choice_materials[2])) {
                GlobalScope.launch(Dispatchers.Main){
                    //레시피 정렬, 보유재료 표시
                    var ready = ""
                    var ready_parts = 0
                    for(i in 0 until materials.size){
                        if(RCP_PARTS_DTLS.contains(materials[i])){
                            ready = ready + " " + materials[i]
                            ready_parts += 1
                        }
                    }
                    RCP_PARTS_DTLS = "[보유 식재료 :"+ ready + "]" +"\n\n"+RCP_PARTS_DTLS
                    itemList.add(ssy_Recipe_Litem(RCP_SEQ, RCP_NM, RCP_PARTS_DTLS,ATT_FILE_NO_MAIN,ready_parts))
                    itemList.sortBy { it.readyparts }
                    itemList.reverse()
                    listAdapter.notifyDataSetChanged()
                    count += 1
                }
            }
        }
        if(count == 0){
            GlobalScope.launch(Dispatchers.Main){
                Toast.makeText(applicationContext, "검색결과가 없습니다.\n재료를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        for(i in 0 until choice_materials.size){
            choice_materials.remove(" ")
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
